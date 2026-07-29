// port-lint: tests server_name.rs
package io.github.kotlinmania.rustlspkitypes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ServerNameTest {
    @Test
    fun testValidation() {
        val tests = listOf(
            Pair("", false),
            Pair("localhost", true),
            Pair("LOCALHOST", true),
            Pair(".localhost", false),
            Pair("..localhost", false),
            Pair("1.2.3.4", false),
            Pair("127.0.0.1", false),
            Pair("absolute.", true),
            Pair("absolute..", false),
            Pair("multiple.labels.absolute.", true),
            Pair("foo.bar.com", true),
            Pair("infix-hyphen-allowed.com", true),
            Pair("-prefixhypheninvalid.com", false),
            Pair("suffixhypheninvalid--", false),
            Pair("suffixhypheninvalid-.com", false),
            Pair("foo.lastlabelendswithhyphen-", false),
            Pair("infix_underscore_allowed.com", true),
            Pair("_prefixunderscorevalid.com", true),
            Pair("labelendswithnumber1.bar.com", true),
            Pair("xn--bcher-kva.example", true),
            Pair("sixtythreesixtythreesixtythreesixtythreesixtythreesixtythreesix.com", true),
            Pair("sixtyfoursixtyfoursixtyfoursixtyfoursixtyfoursixtyfoursixtyfours.com", false),
            Pair("012345678901234567890123456789012345678901234567890123456789012.com", true),
            Pair("0123456789012345678901234567890123456789012345678901234567890123.com", false),
            Pair("01234567890123456789012345678901234567890123456789012345678901-.com", false),
            Pair("012345678901234567890123456789012345678901234567890123456789012-.com", false),
            Pair("numeric-only-final-label.1", false),
            Pair("numeric-only-final-label.absolute.1.", false),
            Pair("1starts-with-number.com", true),
            Pair("1Starts-with-number.com", true),
            Pair("1.2.3.4.com", true),
            Pair("123.numeric-only-first-label", true),
            Pair("a123b.com", true),
            Pair("numeric-only-middle-label.4.com", true),
            Pair("1000-sans.badssl.com", true),
            Pair("twohundredandfiftythreecharacters.twohundredandfiftythreecharacters.twohundredandfiftythreecharacters.twohundredandfiftythreecharacters.twohundredandfiftythreecharacters.twohundredandfiftythreecharacters.twohundredandfiftythreecharacters.twohundredandfi", true),
            Pair("twohundredandfiftyfourcharacters.twohundredandfiftyfourcharacters.twohundredandfiftyfourcharacters.twohundredandfiftyfourcharacters.twohundredandfiftyfourcharacters.twohundredandfiftyfourcharacters.twohundredandfiftyfourcharacters.twohundredandfiftyfourc", false),
        )

        for ((input, expected) in tests) {
            val nameRef = DnsName.tryFrom(input)
            assertEquals(expected, nameRef.isSuccess, "Failed for input: $input")
            val name = DnsName.tryFromString(input)
            assertEquals(expected, name.isSuccess, "Failed for owned input: $input")
        }
    }

    @Test
    fun errorIsDebug() {
        assertEquals("invalid dns name", InvalidDnsNameError.message)
    }

    @Test
    fun dnsNameIsDebug() {
        val example = DnsName.tryFromString("example.com").getOrThrow()
        assertEquals("DnsName(\"example.com\")", example.toString())
    }

    @Test
    fun dnsNameTraits() {
        val example = DnsName.tryFromString("example.com").getOrThrow()
        assertEquals(example, example)
    }

    @Test
    fun tryFromAsciiRejectsBadUtf8() {
        val result = DnsName.tryFrom(byteArrayOf(0x80.toByte()))
        assertTrue(result.isFailure)
    }

    @Test
    fun parseIpv4AddressTest() {
        val validAddresses = listOf(
            Triple("0.0.0.0", byteArrayOf(0, 0, 0, 0), true),
            Triple("1.1.1.1", byteArrayOf(1, 1, 1, 1), true),
            Triple("205.0.0.0", byteArrayOf(205.toByte(), 0, 0, 0), true),
            Triple("0.205.0.0", byteArrayOf(0, 205.toByte(), 0, 0), true),
            Triple("0.0.205.0", byteArrayOf(0, 0, 205.toByte(), 0), true),
            Triple("0.0.0.205", byteArrayOf(0, 0, 0, 205.toByte()), true),
            Triple("0.0.0.20", byteArrayOf(0, 0, 0, 20), true),
        )

        for ((ip, octets, _) in validAddresses) {
            val result = Ipv4Addr.tryFrom(ip)
            assertTrue(result.isSuccess, "Expected success for $ip")
            assertEquals(Ipv4Addr(octets), result.getOrThrow())
        }

        val invalidAddresses = listOf(
            "", "...", ".0.0.0.0", "0.0.0.0.", "0.0.0", "0.0.0.",
            "256.0.0.0", "0.256.0.0", "0.0.256.0", "0.0.0.256",
            "1..1.1.1", "1.1..1.1", "1.1.1..1",
            "025.0.0.0", "0.025.0.0", "0.0.025.0", "0.0.0.025",
            "1234.0.0.0", "0.1234.0.0", "0.0.1234.0", "0.0.0.1234",
        )

        for (ip in invalidAddresses) {
            val result = Ipv4Addr.tryFrom(ip)
            assertTrue(result.isFailure, "Expected failure for $ip")
        }
    }

    @Test
    fun parseIpv6AddressTest() {
        val valid = listOf(
            Pair(
                "2a05:d018:076c:b685:e8ab:afd3:af51:3aed",
                byteArrayOf(
                    0x2a, 0x05, 0xd0.toByte(), 0x18, 0x07, 0x6c, 0xb6.toByte(), 0x85.toByte(),
                    0xe8.toByte(), 0xab.toByte(), 0xaf.toByte(), 0xd3.toByte(), 0xaf.toByte(), 0x51, 0x3a, 0xed.toByte(),
                ),
            ),
            Pair(
                "2A05:D018:076C:B685:E8AB:AFD3:AF51:3AED",
                byteArrayOf(
                    0x2a, 0x05, 0xd0.toByte(), 0x18, 0x07, 0x6c, 0xb6.toByte(), 0x85.toByte(),
                    0xe8.toByte(), 0xab.toByte(), 0xaf.toByte(), 0xd3.toByte(), 0xaf.toByte(), 0x51, 0x3a, 0xed.toByte(),
                ),
            ),
            Pair(
                "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff",
                byteArrayOf(
                    0xff.toByte(), 0xff.toByte(), 0xff.toByte(), 0xff.toByte(),
                    0xff.toByte(), 0xff.toByte(), 0xff.toByte(), 0xff.toByte(),
                    0xff.toByte(), 0xff.toByte(), 0xff.toByte(), 0xff.toByte(),
                    0xff.toByte(), 0xff.toByte(), 0xff.toByte(), 0xff.toByte(),
                ),
            ),
            Pair(
                "FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF",
                byteArrayOf(
                    0xff.toByte(), 0xff.toByte(), 0xff.toByte(), 0xff.toByte(),
                    0xff.toByte(), 0xff.toByte(), 0xff.toByte(), 0xff.toByte(),
                    0xff.toByte(), 0xff.toByte(), 0xff.toByte(), 0xff.toByte(),
                    0xff.toByte(), 0xff.toByte(), 0xff.toByte(), 0xff.toByte(),
                ),
            ),
            Pair(
                "0:0:0:0:0:0:0:0",
                byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
            ),
            Pair(
                "::1",
                byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
            ),
            Pair(
                "::",
                byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
            ),
            Pair(
                "1::",
                byteArrayOf(0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
            ),
            Pair(
                "1:2:3:4:5:6:7:8",
                byteArrayOf(0, 1, 0, 2, 0, 3, 0, 4, 0, 5, 0, 6, 0, 7, 0, 8),
            ),
        )

        for ((ip, octets) in valid) {
            val result = Ipv6Addr.tryFrom(ip)
            assertTrue(result.isSuccess, "Expected success for $ip")
            assertEquals(Ipv6Addr(octets), result.getOrThrow(), "Mismatch for $ip")
        }

        val invalid = listOf(
            "ffgf:ffff:ffff:ffff:ffff:ffff:ffff:ffff",
            "ffff:gfff:ffff:ffff:ffff:ffff:ffff:ffff",
            ":ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff",
            "ffff::ffff:ffff:ffff:ffff:ffff:ffff:ffff",
            "ffff:ffff::ffff:ffff:ffff:ffff:ffff:ffff",
            "ffff:ffff:ffff::ffff:ffff:ffff:ffff:ffff",
            "ffff:ffff:ffff:ffff::ffff:ffff:ffff:ffff",
            "ffff:ffff:ffff:ffff:ffff::ffff:ffff:ffff",
            "ffff:ffff:ffff:ffff:ffff:ffff::ffff:ffff",
            "ffff:ffff:ffff:ffff:ffff:ffff:ffff::ffff",
            "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff:",
            "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff",
            "ga05:d018:076c:b685:e8ab:afd3:af51:3aed",
            ":a05:d018:076c:b685:e8ab:afd3:af51:3aed",
            "2a05:d018:076c:b685:e8ab:afd3:af51:3ae:",
            "2a05:d018:076c:b685:e8ab:afd3:af51:3a::",
            "2a05::018:076c:b685:e8ab:afd3:af51:3aed",
        )

        for (ip in invalid) {
            val result = Ipv6Addr.tryFrom(ip)
            assertTrue(result.isFailure, "Expected failure for $ip")
        }
    }

    @Test
    fun tryFromAsciiIpAddressTest() {
        // Valid IPv4
        val v4Result = IpAddr.tryFrom("127.0.0.1")
        assertTrue(v4Result.isSuccess)
        assertIs<IpAddr.V4>(v4Result.getOrThrow())

        // Invalid IPv4 (falls through to IPv6 parsing, which also fails)
        val invalidResult = IpAddr.tryFrom("127.0.0.")
        assertTrue(invalidResult.isFailure)

        // Valid IPv6
        val v6Result = IpAddr.tryFrom("0000:0000:0000:0000:0000:0000:0000:0001")
        assertTrue(v6Result.isSuccess)
        assertIs<IpAddr.V6>(v6Result.getOrThrow())

        // A hostname is not an IP address
        val hostResult = IpAddr.tryFrom("example.com")
        assertTrue(hostResult.isFailure)
    }

    @Test
    fun serverNameFromDns() {
        val result = ServerName.tryFrom("example.com")
        assertTrue(result.isSuccess)
        assertIs<ServerName.DnsNameValue>(result.getOrThrow())
    }

    @Test
    fun serverNameFromIp() {
        val result = ServerName.tryFrom("127.0.0.1")
        assertTrue(result.isSuccess)
        assertIs<ServerName.IpAddressValue>(result.getOrThrow())
    }

    @Test
    fun serverNameToStr() {
        val dnsName = ServerName.tryFrom("example.com").getOrThrow()
        assertEquals("example.com", dnsName.toStr())

        val ipv4Name = ServerName.tryFrom("127.0.0.1").getOrThrow()
        assertEquals("127.0.0.1", ipv4Name.toStr())

        val ipv6Name = ServerName.tryFrom("::1").getOrThrow()
        assertEquals("::1", ipv6Name.toStr())
    }
}