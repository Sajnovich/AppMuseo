package com.example.appmuseo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

// Regla JUnit: reemplaza Dispatchers.Main (que no existe en la JVM) por Unconfined
// para que viewModelScope funcione igual sin importar el orden en que corren las pruebas.
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(Dispatchers.Unconfined)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
