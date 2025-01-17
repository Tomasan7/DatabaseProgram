package me.tomasan7.opinet.util

import com.microsoft.sqlserver.jdbc.SQLServerException
import com.mysql.cj.jdbc.exceptions.CommunicationsException
import java.net.ConnectException
import java.nio.channels.UnresolvedAddressException

fun Throwable.isNetworkError(): Boolean
{
    return (this is ConnectException) ||
            (this is UnresolvedAddressException) ||
            (this is CommunicationsException) || // MySQL
            (this is SQLServerException && this.sqlState == "08S01") // MSSQL
}
