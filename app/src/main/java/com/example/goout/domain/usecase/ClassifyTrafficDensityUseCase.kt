package com.example.goout.domain.usecase

import com.example.goout.domain.model.DensityStatus
import javax.inject.Inject

/**
 * FUNGSI 1 - Klasifikasi status kepadatan koridor.
 * [OWNER: Bastian] [FUNGSI 1]
 *
 * Aturan:
 *  - speed >= 35 km/jam          -> LANCAR
 *  - 15 <= speed < 35 km/jam     -> MERAYAP
 *  - speed < 15 km/jam           -> MACET
 *  - negatif, NaN, atau tak hingga -> UNKNOWN
 *
 * Test spesifikasi: ClassifyTrafficDensityTest (10 test).
 */
fun classifyTrafficDensity(speedKmh: Double): DensityStatus {
    // TODO(Bastian): implementasikan sesuai aturan di atas.
    return DensityStatus.UNKNOWN
}

class ClassifyTrafficDensityUseCase @Inject constructor() {
    operator fun invoke(speedKmh: Double): DensityStatus = classifyTrafficDensity(speedKmh)
}
