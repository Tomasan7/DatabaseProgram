package me.tomasan7.opinet.report

import kotlinx.serialization.Serializable

@Serializable
data class IntReportDto<T>(
    val entity: T,
    val value: Int
)
