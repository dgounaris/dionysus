package dgounaris.dionysus.storage

import dgounaris.dionysus.storage.user.MemoryUserStorage
import dgounaris.dionysus.storage.user.UserStorage
import org.koin.dsl.module

val storageModule = module {
    single<UserStorage> { MemoryUserStorage() }
}