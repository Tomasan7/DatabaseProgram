package me.tomasan7.opinet

import me.tomasan7.opinet.isolationlevels.IsolationLevels
import me.tomasan7.opinet.util.isNetworkError
import javax.swing.JOptionPane

fun main(args: Array<String>)
{
    if (args.isNotEmpty())
    {
        val isolationLevels = IsolationLevels()
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
        val message = if (e.isNetworkError())
            Messages.networkError
        else
            e.message
        JOptionPane.showMessageDialog(null, message ?: "There was an unknown error", "Error", JOptionPane.ERROR_MESSAGE)
        e.printStackTrace()
    }
}
