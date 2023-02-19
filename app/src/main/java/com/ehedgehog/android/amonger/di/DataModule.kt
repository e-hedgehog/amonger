package com.ehedgehog.android.amonger.di

import com.ehedgehog.android.amonger.screen.PlayersManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataModule {

    @Provides
    @Singleton
    fun provideFirebase(): FirebaseDatabase {
        Firebase.database.setPersistenceEnabled(true)
        return Firebase.database
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return Firebase.storage
    }

    @Provides
    @Singleton
    fun providePlayersReference(database: FirebaseDatabase): DatabaseReference {
        return database.getReference("players")
    }

    @Provides
    @Singleton
    fun provideStorageReference(storage: FirebaseStorage): StorageReference {
        return storage.reference.child("images")
    }

    @Provides
    @Singleton
    fun providePlayersFirebaseManager(databaseReference: DatabaseReference, storageReference: StorageReference): PlayersManager {
        return PlayersManager(databaseReference, storageReference)
    }

}