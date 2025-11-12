package io.oira.fluxeco.core.api

import io.oira.fluxeco.api.IFluxEcoAPI
import io.oira.fluxeco.api.economy.IEconomyManager
import io.oira.fluxeco.api.transaction.ITransactionManager

class FluxEcoAPIImpl(
    private val economyManager: IEconomyManager,
    private val transactionManager: ITransactionManager,
    private val version: String
) : IFluxEcoAPI {

    override fun getEconomyManager(): IEconomyManager = economyManager

    override fun getTransactionManager(): ITransactionManager = transactionManager

    override fun getVersion(): String = version
}

