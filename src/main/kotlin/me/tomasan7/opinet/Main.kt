package me.tomasan7.opinet

import me.tomasan7.opinet.isolationlevels.IsolationLevels
import java.sql.Connection
import javax.swing.JOptionPane

fun main(args: Array<String>)
{
    if (args.isNotEmpty())
    {
        val isolationLevels = IsolationLevels(Connection.TRANSACTION_READ_UNCOMMITTED)
        isolationLevels.start()
        return
    }

    val program = OpiNet()
    try
    {
        program.init()
        program.start()
    }
    catch (e: Exception)
    {
        JOptionPane.showMessageDialog(null, e.message ?: "There was an unknown error", "Error", JOptionPane.ERROR_MESSAGE)
        e.printStackTrace()
    }
}
