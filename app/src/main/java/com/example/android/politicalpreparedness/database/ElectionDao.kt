package com.example.android.politicalpreparedness.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.ElectionAndFollowed
import com.example.android.politicalpreparedness.network.models.FollowedElection

@Dao
interface ElectionDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = Election::class)
    fun insertElections(elections: List<Election>)

    @Query("SELECT * FROM election_table ORDER BY electionDay ASC")
    fun getAllElections(): LiveData<List<Election>>

    @Query("SELECT * FROM election_table WHERE id = :id LIMIT 1")
    suspend fun getElection(id: Int): Election?

    @Query("DELETE FROM election_table")
    suspend fun removeAll()

    //Insert a followed election id
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowed(followed: FollowedElection)

    //Delete a followed election
    @Delete
    suspend fun deleteFollowed(followed: FollowedElection)

    //Get all followed elections
    @Transaction
    @Query("SELECT * FROM election_table JOIN FollowedElection ON election_table.id = FollowedElection.idFollowed ORDER BY electionDay ASC")
    suspend fun getAllFollowedElections(): List<ElectionAndFollowed>

    @Query("SELECT * FROM FollowedElection WHERE idFollowed = :id LIMIT 1")
    suspend fun findElectionId(id: Int): Int?

}