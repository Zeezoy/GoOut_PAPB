package com.example.goout

import com.example.goout.domain.model.DensityStatus
import com.example.goout.domain.usecase.classifyTrafficDensity
import org.junit.Assert.assertEquals
import org.junit.Test

/** CORE FUNCTION 1 — exact threshold tests from the functional spec. */
class ClassifyTrafficDensityTest {

    @Test fun `40 kmh is LANCAR`() = assertEquals(DensityStatus.LANCAR, classifyTrafficDensity(40.0))

    @Test fun `35 kmh is LANCAR`() = assertEquals(DensityStatus.LANCAR, classifyTrafficDensity(35.0))

    @Test fun `34_9 kmh is MERAYAP`() = assertEquals(DensityStatus.MERAYAP, classifyTrafficDensity(34.9))

    @Test fun `20 kmh is MERAYAP`() = assertEquals(DensityStatus.MERAYAP, classifyTrafficDensity(20.0))

    @Test fun `15 kmh is MERAYAP`() = assertEquals(DensityStatus.MERAYAP, classifyTrafficDensity(15.0))

    @Test fun `14_9 kmh is MACET`() = assertEquals(DensityStatus.MACET, classifyTrafficDensity(14.9))

    @Test fun `10 kmh is MACET`() = assertEquals(DensityStatus.MACET, classifyTrafficDensity(10.0))

    @Test fun `0 kmh is MACET`() = assertEquals(DensityStatus.MACET, classifyTrafficDensity(0.0))

    @Test fun `negative speed is UNKNOWN`() = assertEquals(DensityStatus.UNKNOWN, classifyTrafficDensity(-1.0))

    @Test fun `NaN speed is UNKNOWN`() = assertEquals(DensityStatus.UNKNOWN, classifyTrafficDensity(Double.NaN))
}
