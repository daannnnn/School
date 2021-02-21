package com.dan.school

import com.dan.school.other.Utils
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UtilsTest {

    @Test
    fun `empty password returns an error`() {
        val error = Utils.validateBackupPasswordInput("")
        assertThat(error).isEqualTo(Utils.EMPTY_PASSWORD)
    }

    @Test
    fun `password consisting of only spaces returns an error`() {
        val error = Utils.validateBackupPasswordInput("     ")
        assertThat(error).isEqualTo(Utils.BLANK_PASSWORD)
    }

    @Test
    fun `valid password returns null`() {
        val error = Utils.validateBackupPasswordInput("password")
        assertThat(error).isNull()
    }

}
