package io.oira.fluxeco.api

import io.oira.fluxeco.api.economy.IEconomyManager
import io.oira.fluxeco.api.transaction.ITransactionManager

interface IFluxEcoAPI {

    fun getEconomyManager(): IEconomyManager

    fun getTransactionManager(): ITransactionManager

    fun getVersion(): String

    companion object {
        @Volatile
        private var instance: IFluxEcoAPI? = null

        @JvmStatic
        fun getInstance(): IFluxEcoAPI {
            return instance ?: throw IllegalStateException("FluxEco API is not initialized yet")
        }

        @JvmStatic
        fun setInstance(api: IFluxEcoAPI) {
            if (instance != null) {
                throw IllegalStateException("FluxEco API instance already set")
            }
            instance = api
        }

        @JvmStatic
        fun unsetInstance() {
            instance = null
        }
    }
}

