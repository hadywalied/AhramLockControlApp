package com.github.hadywalied.ahramlockcontrolapp

import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import java.time.LocalDateTime

class UtilsKtTest {

    @Test
    fun `test constructSendCommand() returns E for false command`() {
        assertEquals("E", constructSendCommand("FalseArgument"))
    }
    @Test
    fun `test constructSendCommand() returns (Connect,UserMAC) for connect command`() {
        assertEquals("Connect,XX:XX:XX:XX", constructSendCommand("Connect","XX:XX:XX:XX"))
    }
    @Test
    fun `test constructSendCommand() returns (AddUser,UserName,UserMAC) for AddUser command`() {
        assertEquals("AddUser,Hady,XX:XX:XX:XX", constructSendCommand("AddUser","Hady","XX:XX:XX:XX"))
    }
    @Test
    fun `test constructSendCommand() returns (RmUser,UserMAC) for RmUser command`() {
        assertEquals("RmUser,XX:XX:XX:XX", constructSendCommand("RmUser","XX:XX:XX:XX"))
    }
    @Test
    fun `test constructSendCommand() returns (Lock,UserMAC) for Lock command`() {
        assertEquals("Lock,XX:XX:XX:XX", constructSendCommand("Lock","XX:XX:XX:XX"))
    }
    @Test
    fun `test constructSendCommand() returns (UnLock,UserMAC) for UnLock command`() {
        assertEquals("UnLock,XX:XX:XX:XX", constructSendCommand("UnLock","XX:XX:XX:XX"))
    }
    @Test
    fun `test constructSendCommand() returns (GetRecords) for GetRecords command`() {
        assertEquals("GetRecords", constructSendCommand("GetRecords"))
    }
    @Test
    fun `test constructSendCommand() returns (Sync,DateTime) for Sync command`() {
        assertEquals("Sync,${LocalDateTime.now()}", constructSendCommand("Sync",LocalDateTime.now().toString()))
    }

}