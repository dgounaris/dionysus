package dgounaris.dionysus.auth

import org.koin.dsl.module

val authModule = module {
    single<AuthorizationController> { AuthorizationControllerImpl(get(), get()) }
}