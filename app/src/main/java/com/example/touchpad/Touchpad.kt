package com.example.touchpad

import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings

// Desciptors from: http://www.usb.org/developers/hidpage/Hut1_12v2.pdf

object Hid {
    const val ID_TOUCHSCREEN: Byte = 0x1
    const val LOGICAL_SCREEN_MAX = 10000

//    Feature Report: 1 byte (Contact Count Maximum)
//    Input Report:
//    1 byte (Contact Count)
//    1 byte (Contact Identifier)
//    1 byte (Tip Switch and In Range, with constant padding), e.g. 03
//    4 bytes (X and Y Coordinates)
    val MULTI_TOUCHSCREEN_DESCRIPTOR: ByteArray = byteArrayOf(
        0x05.toByte(), 0x0D.toByte(),       // Usage Page (Digitizer)
        0x09.toByte(), 0x04.toByte(),       // Usage (Touch Screen)
        0xA1.toByte(), 0x01.toByte(),       // Collection (Application)
        0x85.toByte(), ID_TOUCHSCREEN,   // REPORT_ID
        0x09.toByte(), 0x55.toByte(),       //   Usage (Contact Count Maximum)
        0x25.toByte(), 0x01.toByte(),       //   Logical Maximum (1)
        0xB1.toByte(), 0x02.toByte(),       //   Feature (Data,Var,Abs,NWrp,Lin,Pref,NNul,NVol,Bit)
        0x09.toByte(), 0x54.toByte(),       //   Usage (Contact Count)
        0x95.toByte(), 0x01.toByte(),       //   Report Count (1)
        0x75.toByte(), 0x08.toByte(),       //   Report Size (8)
        0x81.toByte(), 0x02.toByte(),       //   Input (Data,Var,Abs,NWrp,Lin,Pref,NNul,Bit)
        0x09.toByte(), 0x22.toByte(),       //   Usage (Finger)
        0xA1.toByte(), 0x02.toByte(),       //   Collection (Logical)
        0x09.toByte(), 0x51.toByte(),       //     Usage (Contact Identifier)
        0x75.toByte(), 0x08.toByte(),       //     Report Size (8)
        0x95.toByte(), 0x01.toByte(),       //     Report Count (1)
        0x81.toByte(), 0x02.toByte(),       //     Input (Data,Var,Abs,NWrp,Lin,Pref,NNul,Bit)
        0x09.toByte(), 0x42.toByte(),       //     Usage (Tip Switch)
        0x09.toByte(), 0x32.toByte(),       //     Usage (In Range)
        0x15.toByte(), 0x00.toByte(),       //     Logical Minimum (0)
        0x25.toByte(), 0x01.toByte(),       //     Logical Maximum (1)
        0x75.toByte(), 0x01.toByte(),       //     Report Size (1)
        0x95.toByte(), 0x02.toByte(),       //     Report Count (2)
        0x81.toByte(), 0x02.toByte(),       //     Input (Data,Var,Abs,NWrp,Lin,Pref,NNul,Bit)
        0x95.toByte(), 0x06.toByte(),       //     Report Count (6)
        0x81.toByte(), 0x03.toByte(),       //     Input (Cnst,Var,Abs,NWrp,Lin,Pref,NNul,Bit)
        0x05.toByte(), 0x01.toByte(),       //     Usage Page (Generic Desktop)
        0x09.toByte(), 0x30.toByte(),       //     Usage (X)
        0x09.toByte(), 0x31.toByte(),       //     Usage (Y)
        0x16.toByte(), 0x00.toByte(), 0x00.toByte(), //     Logical Minimum (0)
        0x26.toByte(), 0x10.toByte(), 0x27.toByte(), //     Logical Maximum (10000)
        0x36.toByte(), 0x00.toByte(), 0x00.toByte(), //     Physical Minimum (0)
        0x46.toByte(), 0x10.toByte(), 0x27.toByte(), //     Physical Maximum (10000)
        0x66.toByte(), 0x00.toByte(), 0x00.toByte(), //     Unit (None)
        0x75.toByte(), 0x10.toByte(),       //     Report Size (16)
        0x95.toByte(), 0x02.toByte(),       //     Report Count (2)
        0x81.toByte(), 0x02.toByte(),       //     Input (Data,Var,Abs,NWrp,Lin,Pref,NNul,Bit)
        0xC0.toByte(),                     //   End Collection
        0xC0.toByte()                      // End Collection
    )


    private const val TOUCH_STATE_UP: Byte = 0
    private const val TOUCH_STATE_HOVER: Byte = 2
    private const val TOUCH_STATE_DOWN: Byte = 3

    fun getTouchDown(x: Int, y: Int): ByteArray =
        buildTouchReport(x, y, TOUCH_STATE_DOWN)

    fun getHover(x: Int, y: Int): ByteArray =
        buildTouchReport(x, y, TOUCH_STATE_HOVER)

    fun getTouchUp(x: Int, y: Int): ByteArray =
        buildTouchReport(x, y, TOUCH_STATE_UP)

    private fun buildTouchReport(x: Int, y: Int, state: Byte): ByteArray {
        return byteArrayOf(
            state,
            x.lowByte(),
            x.highByte(),
            y.lowByte(),
            y.highByte()
        )
    }

    private fun Int.lowByte(): Byte = (this and 0xFF).toByte()
    private fun Int.highByte(): Byte = ((this shr 8) and 0xFF).toByte()

    // byte 1   -> "touch" state          (bit 0 = pen up/down, bit 1 = In Range)
    // byte 2,3 -> absolute X coordinate  (0...10000)
    // byte 4,5 -> absolute Y coordinate  (0...10000)
    val TOUCHSCREEN_DESCRIPTOR: ByteArray = byteArrayOf(
        0x05.toByte(), 0x0d.toByte(),  // USAGE_PAGE (Digitizer)
        0x09.toByte(), 0x02.toByte(),  // USAGE (Pen)
        0xa1.toByte(), 0x01.toByte(),  // COLLECTION (Application)
        0x85.toByte(), ID_TOUCHSCREEN, //Report ID
        // Declare a stylus collection

        0x09.toByte(), 0x20.toByte(),  //   Usage (Stylus)
        0xA1.toByte(), 0x00.toByte(),  //   Collection (Physical)
        // Declare a finger touch (finger up/down)

        0x09.toByte(), 0x42.toByte(),  //     Usage (Tip Switch)
        0x09.toByte(), 0x32.toByte(),  //     Usage (In Range)
        0x15.toByte(), 0x00.toByte(),  //     LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x01.toByte(),  //     LOGICAL_MAXIMUM (1)
        0x75.toByte(), 0x01.toByte(),  //     REPORT_SIZE (1)
        0x95.toByte(), 0x02.toByte(),  //     REPORT_COUNT (2)
        0x81.toByte(), 0x02.toByte(),  //     INPUT (Data,Var,Abs)
        // Declare the remaining 6 bits of the first data byte as constant -> the driver will ignore them

        0x75.toByte(), 0x01.toByte(),  //     REPORT_SIZE (1)
        0x95.toByte(), 0x06.toByte(),  //     REPORT_COUNT (6)
        0x81.toByte(), 0x01.toByte(),  //     INPUT (Cnst,Ary,Abs)
        // Define absolute X and Y coordinates of 16 bit each (percent values multiplied with 100)

        0x05.toByte(), 0x01.toByte(),  //     Usage Page (Generic Desktop)
        0x09.toByte(), 0x01.toByte(),  //     Usage (Pointer)
        0xA1.toByte(), 0x00.toByte(),  //     Collection (Physical)
        0x09.toByte(), 0x30.toByte(),  //        Usage (X)
        0x09.toByte(), 0x31.toByte(),  //        Usage (Y)
        0x16.toByte(), 0x00.toByte(), 0x00.toByte(),  //        Logical Minimum (0)
        0x26.toByte(), 0x10.toByte(), 0x27.toByte(),  //        Logical Maximum (10000)
        0x36.toByte(), 0x00.toByte(), 0x00.toByte(),  //        Physical Minimum (0)
        0x46.toByte(), 0x10.toByte(), 0x27.toByte(),  //        Physical Maximum (10000)
        0x66.toByte(), 0x00.toByte(), 0x00.toByte(),  //        UNIT (None)
        0x75.toByte(), 0x10.toByte(),  //        Report Size (16)
        0x95.toByte(), 0x02.toByte(),  //        Report Count (2)
        0x81.toByte(), 0x02.toByte(),  //        Input (Data,Var,Abs)
        0xc0.toByte(),  //     END_COLLECTION

        0xc0.toByte(),  //   END_COLLECTION
        0xc0.toByte() // END_COLLECTION
    )

    private const val SDP_NAME = "Touchpad"
    private const val SDP_DESCRIPTION = "HID touch device"
    private const val SDP_PROVIDER = "None"

    val SDP_TOUCHPAD: BluetoothHidDeviceAppSdpSettings = BluetoothHidDeviceAppSdpSettings(
        SDP_NAME,
        SDP_DESCRIPTION,
        SDP_PROVIDER,
        BluetoothHidDevice.SUBCLASS2_DIGITIZER_TABLET,
        TOUCHSCREEN_DESCRIPTOR
    )

    val QOS_OUT = BluetoothHidDeviceAppQosSettings(
        BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
        800,
        9,
        0,
        11250,
        BluetoothHidDeviceAppQosSettings.MAX
    )
}



