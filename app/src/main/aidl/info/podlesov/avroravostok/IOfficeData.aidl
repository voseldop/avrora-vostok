// IOfficeData.aidl
package info.podlesov.avroravostok;


// Declare any non-default types here with import statements

interface IOfficeData {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    List<String> getCounters();
    String getAccountInfo();
}
