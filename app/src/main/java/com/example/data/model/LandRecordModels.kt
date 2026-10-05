package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RecordType(val code: String, val bengaliName: String, val subtitle: String) {
    CS("CS", "ক্যাডাস্ট্রাল", "১৮৮৮-১৯৪০ জরিপ"),
    SA("SA", "স্টেট একুইজিশন", "১৯৫৬-১৯৬২ রেকর্ড"),
    RS("RS", "রিভিশনাল", "আধুনিক খতিয়ান (Default)"),
    BRS("BRS", "বাংলাদেশ সিটি", "ডিজিটাল বিআরএস")
}

data class Mouza(
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val jlNo: String
)

data class Upazila(
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val mouzas: List<Mouza>
)

data class District(
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val upazilas: List<Upazila>
)

enum class TaskStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    PAUSED
}

data class DownloadTask(
    val id: String,
    val mouzaId: String,
    val mouzaNameBn: String,
    val mouzaNameEn: String,
    val jlNo: String,
    val upazilaNameBn: String,
    val districtNameBn: String,
    val recordType: RecordType,
    val status: TaskStatus = TaskStatus.PENDING,
    val progress: Int = 0, // 0..100
    val totalKhatians: Int = 50,
    val downloadedKhatians: Int = 0,
    val fileSizeBytes: Long = 1024 * 1024,
    val speedKbps: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_land_records")
data class SavedRecord(
    @PrimaryKey val id: String,
    val mouzaId: String,
    val mouzaNameBn: String,
    val mouzaNameEn: String,
    val jlNo: String,
    val upazilaNameBn: String,
    val districtNameBn: String,
    val recordType: String,
    val khatianCount: Int,
    val fileSizeFormatted: String,
    val downloadedAt: Long = System.currentTimeMillis()
)

object LandRecordsDataset {
    val districts: List<District> = listOf(
        District(
            id = "brahmanbaria",
            nameBn = "ব্রাহ্মণবাড়িয়া",
            nameEn = "Brahmanbaria",
            upazilas = listOf(
                Upazila(
                    id = "brahmanbaria-sadar",
                    nameBn = "ব্রাহ্মণবাড়িয়া সদর",
                    nameEn = "Brahmanbaria Sadar",
                    mouzas = listOf(
                        Mouza("bb-sad-01", "মেদ্দা", "Medda", "৪২"),
                        Mouza("bb-sad-02", "কাজীপুর", "Kazipura", "৪৩"),
                        Mouza("bb-sad-03", "পাইকপাড়া", "Paikpara", "৪৪"),
                        Mouza("bb-sad-04", "ঘাতুরা", "Ghatura", "২৮"),
                        Mouza("bb-sad-05", "বীরমপুর", "Birampur", "৩১"),
                        Mouza("bb-sad-06", "সুহিলপুর", "Shuhilpur", "২২"),
                        Mouza("bb-sad-07", "নাতাই", "Natai", "৫৫"),
                        Mouza("bb-sad-08", "মাছিহাতা", "Machihata", "৬১")
                    )
                ),
                Upazila(
                    id = "sarail",
                    nameBn = "সরাইল",
                    nameEn = "Sarail",
                    mouzas = listOf(
                        Mouza("bb-sar-01", "সরাইল সদর", "Sarail Sadar", "১২"),
                        Mouza("bb-sar-02", "কালিকচ্ছ", "Kalikachha", "১৮"),
                        Mouza("bb-sar-03", "নোয়াগাঁও", "Noagaon", "২৫"),
                        Mouza("bb-sar-04", "শাহবাজপুর", "Shahbazpur", "৩২"),
                        Mouza("bb-sar-05", "চুন্টা", "Chunta", "১৫"),
                        Mouza("bb-sar-06", "পানিশ্বর", "Panishwar", "৪০"),
                        Mouza("bb-sar-07", "পাকশিমুল", "Pakshimul", "৪৮")
                    )
                ),
                Upazila(
                    id = "ashuganj",
                    nameBn = "আশুগঞ্জ",
                    nameEn = "Ashuganj",
                    mouzas = listOf(
                        Mouza("bb-ash-01", "আশুগঞ্জ", "Ashuganj", "০৪"),
                        Mouza("bb-ash-02", "চর চারতলা", "Char Chartala", "০৮"),
                        Mouza("bb-ash-03", "দুর্গাপুর", "Durgapur", "১১"),
                        Mouza("bb-ash-04", "তালশহর পশ্চিম", "Talshahar Paschim", "১৪"),
                        Mouza("bb-ash-05", "সোহাগপুর", "Soagpur", "১৯"),
                        Mouza("bb-ash-06", "আড়াইসিধা", "Araisidha", "২৩")
                    )
                ),
                Upazila(
                    id = "kasba",
                    nameBn = "কসবা",
                    nameEn = "Kasba",
                    mouzas = listOf(
                        Mouza("bb-kas-01", "কসবা", "Kasba", "১৫"),
                        Mouza("bb-kas-02", "কুটি", "Kuti", "০২"),
                        Mouza("bb-kas-03", "কায়েমপুর", "Kayempur", "২২"),
                        Mouza("bb-kas-04", "বাদৈর", "Badair", "৩৪"),
                        Mouza("bb-kas-05", "বায়েখ", "Bayek", "৪১"),
                        Mouza("bb-kas-06", "খাড়েরা", "Kharera", "২৯"),
                        Mouza("bb-kas-07", "মেহারী", "Mehari", "১৮")
                    )
                ),
                Upazila(
                    id = "nabinagar",
                    nameBn = "নবীনগর",
                    nameEn = "Nabinagar",
                    mouzas = listOf(
                        Mouza("bb-nab-01", "নবীনগর সদর", "Nabinagar Sadar", "০১"),
                        Mouza("bb-nab-02", "বিদ্যাকুট", "Biddakut", "০৯"),
                        Mouza("bb-nab-03", "শিবপুর", "Shibpur", "১৬"),
                        Mouza("bb-nab-04", "বিটঘর", "Bitghar", "২৪"),
                        Mouza("bb-nab-05", "কাইতলা", "Kaitala", "৩৩"),
                        Mouza("bb-nab-06", "সলিমগঞ্জ", "Salimganj", "৪৫"),
                        Mouza("bb-nab-07", "জিনোদপুর", "Jinodpur", "৫২")
                    )
                ),
                Upazila(
                    id = "nasirnagar",
                    nameBn = "নাসিরনগর",
                    nameEn = "Nasirnagar",
                    mouzas = listOf(
                        Mouza("bb-nas-01", "নাসিরনগর", "Nasirnagar", "০৫"),
                        Mouza("bb-nas-02", "ফান্দাউক", "Fandauk", "১১"),
                        Mouza("bb-nas-03", "চাতলপাড়", "Chatalpar", "১৮"),
                        Mouza("bb-nas-04", "হরিপুর", "Haripur", "২২"),
                        Mouza("bb-nas-05", "বুড়িশ্বর", "Burishwar", "২৯"),
                        Mouza("bb-nas-06", "কুন্ডা", "Kunda", "৩৫"),
                        Mouza("bb-nas-07", "গোকর্ণ", "Gokarna", "৪১")
                    )
                ),
                Upazila(
                    id = "bancharampur",
                    nameBn = "বাঞ্ছারামপুর",
                    nameEn = "Bancharampur",
                    mouzas = listOf(
                        Mouza("bb-ban-01", "বাঞ্ছারামপুর সদর", "Bancharampur Sadar", "০৩"),
                        Mouza("bb-ban-02", "উজানচর", "Ujanchar", "০৭"),
                        Mouza("bb-ban-03", "দরিয়াদৌলত", "Dariyadaulat", "১৪"),
                        Mouza("bb-ban-04", "মানিকপুর", "Manikpur", "২১"),
                        Mouza("bb-ban-05", "সলিমাবাদ", "Salimabad", "২৮"),
                        Mouza("bb-ban-06", "তেজখালী", "Tejkhali", "৩৫"),
                        Mouza("bb-ban-07", "পাহাড়িয়াকান্দি", "Pahariakandi", "৪২")
                    )
                ),
                Upazila(
                    id = "akhaura",
                    nameBn = "আখাউড়া",
                    nameEn = "Akhaura",
                    mouzas = listOf(
                        Mouza("bb-akh-01", "আখাউড়া পৌরসভা", "Akhaura Municipality", "০২"),
                        Mouza("bb-akh-02", "মোগড়া", "Mogra", "১০"),
                        Mouza("bb-akh-03", "মণিয়ন্দ", "Moniyond", "১৬"),
                        Mouza("bb-akh-04", "ধরখার", "Dharkhar", "২৪"),
                        Mouza("bb-akh-05", "গঙ্গাসাগর", "Gangasagar", "০৮"),
                        Mouza("bb-akh-06", "নূরপুর", "Noorpur", "১৯")
                    )
                ),
                Upazila(
                    id = "bijoynagar",
                    nameBn = "বিজয়নগর",
                    nameEn = "Bijoynagar",
                    mouzas = listOf(
                        Mouza("bb-bij-01", "চান্দুরা", "Chandura", "০৬"),
                        Mouza("bb-bij-02", "সিঙ্গারবিল", "Singerbil", "১২"),
                        Mouza("bb-bij-03", "হরষপুর", "Harashpur", "১৯"),
                        Mouza("bb-bij-04", "বুধন্তী", "Budhanti", "২৭"),
                        Mouza("bb-bij-05", "ইছাপুরা", "Ichhapur", "৩৪"),
                        Mouza("bb-bij-06", "চম্পকনগর", "Champaknagar", "৪১"),
                        Mouza("bb-bij-07", "পাহাড়পুর", "Paharpur", "৪৮")
                    )
                )
            )
        ),
        District(
            id = "cumilla",
            nameBn = "কুমিল্লা",
            nameEn = "Cumilla",
            upazilas = listOf(
                Upazila(
                    id = "adarsha-sadar",
                    nameBn = "আদর্শ সদর",
                    nameEn = "Adarsha Sadar",
                    mouzas = listOf(
                        Mouza("cu-ada-01", "শাশনগাছা", "Shashan Gachha", "১৪"),
                        Mouza("cu-ada-02", "ছাতিপট্টি", "Chhatipatti", "১৮"),
                        Mouza("cu-ada-03", "বাদুড়তলা", "Badurtala", "২২"),
                        Mouza("cu-ada-04", "বাগিচাগাঁও", "Bagichagaon", "২৭"),
                        Mouza("cu-ada-05", "জগন্নাথপুর", "Jagannathpur", "৩৫"),
                        Mouza("cu-ada-06", "আমড়াতলী", "Amratali", "৪৮"),
                        Mouza("cu-ada-07", "পাঁচথুবী", "Panchthubi", "৫৪")
                    )
                ),
                Upazila(
                    id = "sadar-dakshin",
                    nameBn = "সদর দক্ষিণ",
                    nameEn = "Sadar Dakshin",
                    mouzas = listOf(
                        Mouza("cu-sdk-01", "বিজয়পুর", "Bijoypur", "১২"),
                        Mouza("cu-sdk-02", "চৌয়ারা", "Chowara", "১৯"),
                        Mouza("cu-sdk-03", "গোপীনাথপুর", "Gopinathpur", "২৬"),
                        Mouza("cu-sdk-04", "গোলাবাড়ি", "Golabari", "৩৩"),
                        Mouza("cu-sdk-05", "বাড়পাড়া", "Barapara", "৪০"),
                        Mouza("cu-sdk-06", "পেরুল", "Perul", "৪৭")
                    )
                ),
                Upazila(
                    id = "chandina",
                    nameBn = "চান্দিনা",
                    nameEn = "Chandina",
                    mouzas = listOf(
                        Mouza("cu-cha-01", "চান্দিনা পৌরসভা", "Chandina Pouroshova", "০৮"),
                        Mouza("cu-cha-02", "মাধাইয়া", "Madhaiya", "১৫"),
                        Mouza("cu-cha-03", "বরকইট", "Barkait", "২৩"),
                        Mouza("cu-cha-04", "মাইজখার", "Maijkhar", "৩১"),
                        Mouza("cu-cha-05", "কেরণখাল", "Kerankhal", "৩৯"),
                        Mouza("cu-cha-06", "গল্লাই", "Gallai", "৪৬"),
                        Mouza("cu-cha-07", "বাতাগাসী", "Bataghashi", "৫২")
                    )
                ),
                Upazila(
                    id = "daudkandi",
                    nameBn = "দাউদকান্দি",
                    nameEn = "Daudkandi",
                    mouzas = listOf(
                        Mouza("cu-dau-01", "দাউদকান্দি সদর", "Daudkandi Sadar", "০৫"),
                        Mouza("cu-dau-02", "গৌরীপুর", "Gouripur", "১৪"),
                        Mouza("cu-dau-03", "ইলিয়টগঞ্জ", "Eliotganj", "২২"),
                        Mouza("cu-dau-04", "সুন্দলপুর", "Sundalpur", "২৯"),
                        Mouza("cu-dau-05", "জিংলাতলী", "Jinglatali", "৩৬"),
                        Mouza("cu-dau-06", "মারুকা", "Maruka", "৪৩"),
                        Mouza("cu-dau-07", "গোয়ালমারী", "Goalmari", "৫০")
                    )
                ),
                Upazila(
                    id = "debidwar",
                    nameBn = "দেবিদ্বার",
                    nameEn = "Debidwar",
                    mouzas = listOf(
                        Mouza("cu-deb-01", "দেবিদ্বার পৌরসভা", "Debidwar Pouro", "১০"),
                        Mouza("cu-deb-02", "মোহনপুর", "Mohanpur", "১৭"),
                        Mouza("cu-deb-03", "রসুল্লাবাদ", "Rasullabad", "২৫"),
                        Mouza("cu-deb-04", "গুনাইঘর", "Gunaighar", "৩২"),
                        Mouza("cu-deb-05", "ধামতী", "Dhamti", "৪১"),
                        Mouza("cu-deb-06", "জাফরগঞ্জ", "Jafarganj", "৪৯")
                    )
                ),
                Upazila(
                    id = "burichang",
                    nameBn = "বুড়িচং",
                    nameEn = "Burichang",
                    mouzas = listOf(
                        Mouza("cu-bur-01", "বুড়িচং সদর", "Burichang Sadar", "০৭"),
                        Mouza("cu-bur-02", "ময়নামতি", "Mainamati", "২২"),
                        Mouza("cu-bur-03", "পীরযাত্রাপুর", "Pirjatrapur", "১৫"),
                        Mouza("cu-bur-04", "বাকশীমূল", "Bakshimul", "৩১"),
                        Mouza("cu-bur-05", "মোকাম", "Mokam", "৩৮"),
                        Mouza("cu-bur-06", "রাজাপুর", "Rajapur", "৪৫")
                    )
                ),
                Upazila(
                    id = "brahmanpara",
                    nameBn = "ব্রাহ্মণপাড়া",
                    nameEn = "Brahmanpara",
                    mouzas = listOf(
                        Mouza("cu-brp-01", "ব্রাহ্মণপাড়া সদর", "Brahmanpara Sadar", "০৪"),
                        Mouza("cu-brp-02", "মাধবপুর", "Madhabpur", "১১"),
                        Mouza("cu-brp-03", "শিদলাই", "Shidlai", "১৯"),
                        Mouza("cu-brp-04", "চান্দলা", "Chandla", "২৬"),
                        Mouza("cu-brp-05", "শশীদল", "Shashidal", "৩৪"),
                        Mouza("cu-brp-06", "দুলালপুর", "Dulalpur", "৪২")
                    )
                ),
                Upazila(
                    id = "chauddagram",
                    nameBn = "চৌদ্দগ্রাম",
                    nameEn = "Chauddagram",
                    mouzas = listOf(
                        Mouza("cu-chaud-01", "চৌদ্দগ্রাম বাজার", "Chauddagram Bazar", "০৬"),
                        Mouza("cu-chaud-02", "মিয়াবাজার", "Miabazar", "১৩"),
                        Mouza("cu-chaud-03", "কাশীনগর", "Kashinagar", "২১"),
                        Mouza("cu-chaud-04", "বাতিসা", "Batisa", "২৯"),
                        Mouza("cu-chaud-05", "মুন্সীরহাট", "Munshirhat", "৩৭"),
                        Mouza("cu-chaud-06", "গুণবতী", "Gunabati", "৪৪"),
                        Mouza("cu-chaud-07", "চিওড়া", "Cheora", "৫১")
                    )
                ),
                Upazila(
                    id = "laksam",
                    nameBn = "লাকসাম",
                    nameEn = "Laksam",
                    mouzas = listOf(
                        Mouza("cu-lak-01", "লাকসাম পৌরসভা", "Laksam Pouro", "০৩"),
                        Mouza("cu-lak-02", "কান্দিরপাড়", "Kandirpar", "১০"),
                        Mouza("cu-lak-03", "গোবিন্দপুর", "Gobindapur", "১৮"),
                        Mouza("cu-lak-04", "উত্তরদা", "Uttarda", "২৫"),
                        Mouza("cu-lak-05", "মুদাফফরগঞ্জ", "Mudhafurganj", "৪২")
                    )
                ),
                Upazila(
                    id = "muradnagar",
                    nameBn = "মুরাদনগর",
                    nameEn = "Muradnagar",
                    mouzas = listOf(
                        Mouza("cu-mur-01", "মুরাদনগর সদর", "Muradnagar Sadar", "০৯"),
                        Mouza("cu-mur-02", "কোম্পানীগঞ্জ", "Companyganj", "১৬"),
                        Mouza("cu-mur-03", "বাঙ্গরা", "Bangora", "২৪"),
                        Mouza("cu-mur-04", "জাহাপুর", "Jahapur", "৩৩"),
                        Mouza("cu-mur-05", "রামচন্দ্রপুর", "Ramchandrapur", "৪১"),
                        Mouza("cu-mur-06", "শ্রীকাইল", "Sreekail", "৫০")
                    )
                ),
                Upazila(
                    id = "barura",
                    nameBn = "বরুড়া",
                    nameEn = "Barura",
                    mouzas = listOf(
                        Mouza("cu-bar-01", "বরুড়া পৌরসভা", "Barura Pouro", "০৫"),
                        Mouza("cu-bar-02", "গালিমপুর", "Galimpur", "১২"),
                        Mouza("cu-bar-03", "শিলমুড়ী", "Shilmuri", "২০"),
                        Mouza("cu-bar-04", "পায়েলগাছা", "Payalgacha", "২৮"),
                        Mouza("cu-bar-05", "আড্ডা", "Adda", "৩৫"),
                        Mouza("cu-bar-06", "শাকপুর", "Shakpur", "৪৩")
                    )
                ),
                Upazila(
                    id = "homna",
                    nameBn = "হোমনা",
                    nameEn = "Homna",
                    mouzas = listOf(
                        Mouza("cu-hom-01", "হোমনা সদর", "Homna Sadar", "০২"),
                        Mouza("cu-hom-02", "আসাদপুর", "Asadpur", "০৮"),
                        Mouza("cu-hom-03", "জয়পুর", "Joypur", "১৫"),
                        Mouza("cu-hom-04", "ঘাগুটিয়া", "Ghagutia", "৩০"),
                        Mouza("cu-hom-05", "মাথাভাঙ্গা", "Mathabhanga", "৪৬")
                    )
                ),
                Upazila(
                    id = "titas",
                    nameBn = "তিতাস",
                    nameEn = "Titas",
                    mouzas = listOf(
                        Mouza("cu-tit-01", "মজিদপুর", "Majidpur", "০৪"),
                        Mouza("cu-tit-02", "বলরামপুর", "Balrampur", "১১"),
                        Mouza("cu-tit-03", "জগতপুর", "Jagatpur", "১৯"),
                        Mouza("cu-tit-04", "কড়িকান্দি", "Karikandi", "২৭"),
                        Mouza("cu-tit-05", "জিয়ারকান্দি", "Zearkandi", "৪১")
                    )
                ),
                Upazila(
                    id = "meghna",
                    nameBn = "মেঘনা",
                    nameEn = "Meghna",
                    mouzas = listOf(
                        Mouza("cu-meg-01", "মানিকরচর", "Manikar Char", "০৩"),
                        Mouza("cu-meg-02", "চন্দনপুর", "Chandanpur", "০৯"),
                        Mouza("cu-meg-03", "চালিভাঙ্গা", "Chalibhanga", "১৬"),
                        Mouza("cu-meg-04", "গোবিন্দপুর", "Gobindapur", "২৪"),
                        Mouza("cu-meg-05", "রাধানগর", "Radhanagar", "৩১")
                    )
                ),
                Upazila(
                    id = "monohargonj",
                    nameBn = "মনোহরগঞ্জ",
                    nameEn = "Monohargonj",
                    mouzas = listOf(
                        Mouza("cu-mon-01", "মনোহরগঞ্জ সদর", "Monohargonj Sadar", "০৫"),
                        Mouza("cu-mon-02", "বাইশগাঁও", "Baishgaon", "১২"),
                        Mouza("cu-mon-03", "সরসপুর", "Sarashpur", "১৯"),
                        Mouza("cu-mon-04", "হাসনাবাদ", "Hasnabad", "২৭"),
                        Mouza("cu-mon-05", "ঝালম", "Jhalam", "৩৪")
                    )
                ),
                Upazila(
                    id = "nangalkot",
                    nameBn = "নাঙ্গলকোট",
                    nameEn = "Nangalkot",
                    mouzas = listOf(
                        Mouza("cu-nan-01", "নাঙ্গলকোট পৌরসভা", "Nangalkot Pouro", "০৬"),
                        Mouza("cu-nan-02", "ঢালুয়া", "Dhalua", "১৪"),
                        Mouza("cu-nan-03", "বক্সগঞ্জ", "Boxoganj", "২১"),
                        Mouza("cu-nan-04", "মোকরা", "Mokara", "২৯"),
                        Mouza("cu-nan-05", "পেরিয়া", "Peria", "৩৭"),
                        Mouza("cu-nan-06", "রায়কোট", "Roykot", "৪৫")
                    )
                ),
                Upazila(
                    id = "lalmai",
                    nameBn = "লালমাই",
                    nameEn = "Lalmai",
                    mouzas = listOf(
                        Mouza("cu-lal-01", "বাগমারা", "Bagmara", "০৮"),
                        Mouza("cu-lal-02", "ভুলাইণ", "Bhulain", "১৫"),
                        Mouza("cu-lal-03", "বেলঘর", "Belghar", "২৩"),
                        Mouza("cu-lal-04", "পেরুল দক্ষিণ", "Perul Dakshin", "৩১"),
                        Mouza("cu-lal-05", "বাকই উত্তর", "Bakoi Uttar", "৩৯")
                    )
                )
            )
        )
    )
}
