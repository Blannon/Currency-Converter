package com.blannonnetwork.currencyconveter.di

import com.blannonnetwork.currencyconveter.BuildConfig
import com.blannonnetwork.currencyconveter.data.ExchangeRepositoryImpl
import com.blannonnetwork.currencyconveter.domain.ConvertUseCase
import com.blannonnetwork.currencyconveter.domain.ExchangeRepository
import com.blannonnetwork.currencyconveter.presentation.ExchangeViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {

    singleOf(:: ExchangeRepositoryImpl).bind<ExchangeRepository>()

    single { ConvertUseCase(get()) }

    viewModel { ExchangeViewModel(get(), get()) }

    single {
        // Provide HttpClient from centralized provider in data.network
        com.blannonnetwork.currencyconveter.data.network.KtorClientProvider.create()
    }
}
