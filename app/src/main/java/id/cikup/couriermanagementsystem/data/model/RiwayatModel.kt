package id.cikup.couriermanagementsystem.data.model

data class RiwayatModel(
        val aToBStatus: String = "Selesai",
        val aToBRange: Int = 20,
        val bToCStatus: String = "Selesai",
        val bToCRange: Int = 40,
        val reimburse1Status: String = "Proses",
        val reimburse1Type: String = "Tol",
        val reimburse2Status: String = "Batalkan",
        val reimburse2Type: String = "Bensin",
        val date:String = "29 Desember 2020"
)