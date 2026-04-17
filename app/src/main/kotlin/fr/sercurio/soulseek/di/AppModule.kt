package fr.sercurio.soulseek.di

import fr.sercurio.soulseek.SoulseekClient
import fr.sercurio.soulseek.presentation.login.LoginViewModel
import fr.sercurio.soulseek.presentation.rooms.RoomsViewModel
import fr.sercurio.soulseek.presentation.search.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
  single<SoulseekClient> { SoulseekClient.Companion() }

  viewModel { LoginViewModel(get()) }
  viewModel { SearchViewModel(get()) }
  viewModel { RoomsViewModel(get()) }
}
