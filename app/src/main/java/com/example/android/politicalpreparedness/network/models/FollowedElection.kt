package com.example.android.politicalpreparedness.network.models

import androidx.room.*
import com.squareup.moshi.Json
import java.util.*

@Entity
data class FollowedElection(
        @PrimaryKey val idFollowed: Int
)

data class ElectionAndFollowed(
        @Embedded val election: Election,
        @Relation(
            parentColumn = "id",
            entityColumn = "idFollowed"
        )
        val followed: FollowedElection
)