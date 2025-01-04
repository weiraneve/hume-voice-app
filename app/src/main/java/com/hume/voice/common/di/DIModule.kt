package com.hume.voice.common.di

import com.hume.voice.voice.HumeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { HumeViewModel() }
}
