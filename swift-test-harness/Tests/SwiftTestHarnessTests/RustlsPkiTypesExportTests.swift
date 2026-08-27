#if canImport(Testing)
import Testing
import RustlsPkiTypes

@Suite("RustlsPkiTypes Swift Export Suite")
struct RustlsPkiTypesExportTests {
    @Test("Swift module loads cleanly")
    func swiftModuleLoads() {
        #expect(Bool(true), "RustlsPkiTypes swift module imported cleanly")
    }
}
#elseif canImport(XCTest)
import XCTest
import RustlsPkiTypes

final class RustlsPkiTypesExportTests: XCTestCase {
    func testSwiftModuleLoads() {
        XCTAssertTrue(true, "RustlsPkiTypes swift module imported cleanly")
    }
}
#endif
