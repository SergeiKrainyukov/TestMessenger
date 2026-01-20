package com.skrainyukov.testmessenger.di

import com.skrainyukov.testmessenger.data.repository.AuthRepositoryImpl
import com.skrainyukov.testmessenger.data.repository.UserRepositoryImpl
import com.skrainyukov.testmessenger.domain.repository.AuthRepository
import com.skrainyukov.testmessenger.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}