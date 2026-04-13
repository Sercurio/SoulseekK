package fr.sercurio.soulseek

import fr.sercurio.soulseek.login.LoginViewModel
import fr.sercurio.soulseek.rooms.RoomsViewModel
import fr.sercurio.soulseek.search.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<SoulseekClient> { SoulseekClient() }

    viewModel { LoginViewModel(get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { RoomsViewModel(get()) }
}
