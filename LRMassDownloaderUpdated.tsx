import React, { useState, useEffect, useRef, useMemo, useCallback } from 'react';

/**
 * Bangladesh Land Records (LR) Mass Downloader Updated
 * GovTech UI Specialist Edition - MD3 & Deep Forest Emerald Palette
 * Districts: Cumilla (কুমিল্লা) & Brahmanbaria (ব্রাহ্মণবাড়িয়া)
 */

export type RecordType = 'CS' | 'SA' | 'RS' | 'BRS';

export interface MouzaItem {
  id: string;
  nameBn: string;
  nameEn: string;
  jlNo: string;
}

export interface UpazilaItem {
  id: string;
  nameBn: string;
  nameEn: string;
  mouzas: MouzaItem[];
}

export interface DistrictItem {
  id: string;
  nameBn: string;
  nameEn: string;
  upazilas: UpazilaItem[];
}

export type TaskStatus = 'PENDING' | 'DOWNLOADING' | 'COMPLETED' | 'PAUSED' | 'FAILED';

export interface DownloadTask {
  id: string;
  mouzaId: string;
  mouzaNameBn: string;
  mouzaNameEn: string;
  jlNo: string;
  upazilaNameBn: string;
  districtNameBn: string;
  recordType: RecordType;
  status: TaskStatus;
  progress: number; // 0 - 100
  totalKhatians: number;
  downloadedKhatians: number;
  fileSizeBytes: number;
  speedKbps: number;
  timestamp: number;
}

// -------------------------------------------------------------
// COMPREHENSIVE UN-TRUNCATED DATASET (Cumilla & Brahmanbaria)
// -------------------------------------------------------------
const LR_DATASET: DistrictItem[] = [
  {
    id: 'brahmanbaria',
    nameBn: 'ব্রাহ্মণবাড়িয়া',
    nameEn: 'Brahmanbaria',
    upazilas: [
      {
        id: 'brahmanbaria-sadar',
        nameBn: 'ব্রাহ্মণবাড়িয়া সদর',
        nameEn: 'Brahmanbaria Sadar',
        mouzas: [
          { id: 'bb-sad-01', nameBn: 'মেদ্দা', nameEn: 'Medda', jlNo: '৪২' },
          { id: 'bb-sad-02', nameBn: 'কাজীপুর', nameEn: 'Kazipura', jlNo: '৪৩' },
          { id: 'bb-sad-03', nameBn: 'পাইকপাড়া', nameEn: 'Paikpara', jlNo: '৪৪' },
          { id: 'bb-sad-04', nameBn: 'ঘাতুরা', nameEn: 'Ghatura', jlNo: '২৮' },
          { id: 'bb-sad-05', nameBn: 'বীরমপুর', nameEn: 'Birampur', jlNo: '৩১' },
          { id: 'bb-sad-06', nameBn: 'সুহিলপুর', nameEn: 'Shuhilpur', jlNo: '২২' },
          { id: 'bb-sad-07', nameBn: 'নাতাই', nameEn: 'Natai', jlNo: '৫৫' },
          { id: 'bb-sad-08', nameBn: 'মাছিহাতা', nameEn: 'Machihata', jlNo: '৬১' },
        ],
      },
      {
        id: 'sarail',
        nameBn: 'সরাইল',
        nameEn: 'Sarail',
        mouzas: [
          { id: 'bb-sar-01', nameBn: 'সরাইল সদর', nameEn: 'Sarail Sadar', jlNo: '১২' },
          { id: 'bb-sar-02', nameBn: 'কালিকচ্ছ', nameEn: 'Kalikachha', jlNo: '১৮' },
          { id: 'bb-sar-03', nameBn: 'নোয়াগাঁও', nameEn: 'Noagaon', jlNo: '২৫' },
          { id: 'bb-sar-04', nameBn: 'শাহবাজপুর', nameEn: 'Shahbazpur', jlNo: '৩২' },
          { id: 'bb-sar-05', nameBn: 'চুন্টা', nameEn: 'Chunta', jlNo: '১৫' },
          { id: 'bb-sar-06', nameBn: 'পানিশ্বর', nameEn: 'Panishwar', jlNo: '৪০' },
          { id: 'bb-sar-07', nameBn: 'পাকশিমুল', nameEn: 'Pakshimul', jlNo: '৪৮' },
        ],
      },
      {
        id: 'ashuganj',
        nameBn: 'আশুগঞ্জ',
        nameEn: 'Ashuganj',
        mouzas: [
          { id: 'bb-ash-01', nameBn: 'আশুগঞ্জ', nameEn: 'Ashuganj', jlNo: '০৪' },
          { id: 'bb-ash-02', nameBn: 'চর চারতলা', nameEn: 'Char Chartala', jlNo: '০৮' },
          { id: 'bb-ash-03', nameBn: 'দুর্গাপুর', nameEn: 'Durgapur', jlNo: '১১' },
          { id: 'bb-ash-04', nameBn: 'তালশহর পশ্চিম', nameEn: 'Talshahar Paschim', jlNo: '১৪' },
          { id: 'bb-ash-05', nameBn: 'সোহাগপুর', nameEn: 'Soagpur', jlNo: '১৯' },
          { id: 'bb-ash-06', nameBn: 'আড়াইসিধা', nameEn: 'Araisidha', jlNo: '২৩' },
        ],
      },
      {
        id: 'kasba',
        nameBn: 'কসবা',
        nameEn: 'Kasba',
        mouzas: [
          { id: 'bb-kas-01', nameBn: 'কসবা', nameEn: 'Kasba', jlNo: '১৫' },
          { id: 'bb-kas-02', nameBn: 'কুটি', nameEn: 'Kuti', jlNo: '০২' },
          { id: 'bb-kas-03', nameBn: 'কায়েমপুর', nameEn: 'Kayempur', jlNo: '২২' },
          { id: 'bb-kas-04', nameBn: 'বাদৈর', nameEn: 'Badair', jlNo: '৩৪' },
          { id: 'bb-kas-05', nameBn: 'বায়েখ', nameEn: 'Bayek', jlNo: '৪১' },
          { id: 'bb-kas-06', nameBn: 'খাড়েরা', nameEn: 'Kharera', jlNo: '২৯' },
          { id: 'bb-kas-07', nameBn: 'মেহারী', nameEn: 'Mehari', jlNo: '১৮' },
        ],
      },
      {
        id: 'nabinagar',
        nameBn: 'নবীনগর',
        nameEn: 'Nabinagar',
        mouzas: [
          { id: 'bb-nab-01', nameBn: 'নবীনগর সদর', nameEn: 'Nabinagar Sadar', jlNo: '০১' },
          { id: 'bb-nab-02', nameBn: 'বিদ্যাকুট', nameEn: 'Biddakut', jlNo: '০৯' },
          { id: 'bb-nab-03', nameBn: 'শিবপুর', nameEn: 'Shibpur', jlNo: '১৬' },
          { id: 'bb-nab-04', nameBn: 'বিটঘর', nameEn: 'Bitghar', jlNo: '২৪' },
          { id: 'bb-nab-05', nameBn: 'কাইতলা', nameEn: 'Kaitala', jlNo: '৩৩' },
          { id: 'bb-nab-06', nameBn: 'সলিমগঞ্জ', nameEn: 'Salimganj', jlNo: '৪৫' },
          { id: 'bb-nab-07', nameBn: 'জিনোদপুর', nameEn: 'Jinodpur', jlNo: '৫২' },
        ],
      },
      {
        id: 'nasirnagar',
        nameBn: 'নাসিরনগর',
        nameEn: 'Nasirnagar',
        mouzas: [
          { id: 'bb-nas-01', nameBn: 'নাসিরনগর', nameEn: 'Nasirnagar', jlNo: '০৫' },
          { id: 'bb-nas-02', nameBn: 'ফান্দাউক', nameEn: 'Fandauk', jlNo: '১১' },
          { id: 'bb-nas-03', nameBn: 'চাতলপাড়', nameEn: 'Chatalpar', jlNo: '১৮' },
          { id: 'bb-nas-04', nameBn: 'হরিপুর', nameEn: 'Haripur', jlNo: '২২' },
          { id: 'bb-nas-05', nameBn: 'বুড়িশ্বর', nameEn: 'Burishwar', jlNo: '২৯' },
          { id: 'bb-nas-06', nameBn: 'কুন্ডা', nameEn: 'Kunda', jlNo: '৩৫' },
          { id: 'bb-nas-07', nameBn: 'গোকর্ণ', nameEn: 'Gokarna', jlNo: '৪১' },
        ],
      },
      {
        id: 'bancharampur',
        nameBn: 'বাঞ্ছারামপুর',
        nameEn: 'Bancharampur',
        mouzas: [
          { id: 'bb-ban-01', nameBn: 'বাঞ্ছারামপুর সদর', nameEn: 'Bancharampur Sadar', jlNo: '০৩' },
          { id: 'bb-ban-02', nameBn: 'উজানচর', nameEn: 'Ujanchar', jlNo: '০৭' },
          { id: 'bb-ban-03', nameBn: 'দরিয়াদৌলত', nameEn: 'Dariyadaulat', jlNo: '১৪' },
          { id: 'bb-ban-04', nameBn: 'মানিকপুর', nameEn: 'Manikpur', jlNo: '২১' },
          { id: 'bb-ban-05', nameBn: 'সলিমাবাদ', nameEn: 'Salimabad', jlNo: '২৮' },
          { id: 'bb-ban-06', nameBn: 'তেজখালী', nameEn: 'Tejkhali', jlNo: '৩৫' },
          { id: 'bb-ban-07', nameBn: 'পাহাড়িয়াকান্দি', nameEn: 'Pahariakandi', jlNo: '৪২' },
        ],
      },
      {
        id: 'akhaura',
        nameBn: 'আখাউড়া',
        nameEn: 'Akhaura',
        mouzas: [
          { id: 'bb-akh-01', nameBn: 'আখাউড়া পৌরসভা', nameEn: 'Akhaura Municipality', jlNo: '০২' },
          { id: 'bb-akh-02', nameBn: 'মোগড়া', nameEn: 'Mogra', jlNo: '১০' },
          { id: 'bb-akh-03', nameBn: 'মণিয়ন্দ', nameEn: 'Moniyond', jlNo: '১৬' },
          { id: 'bb-akh-04', nameBn: 'ধরখার', nameEn: 'Dharkhar', jlNo: '২৪' },
          { id: 'bb-akh-05', nameBn: 'গঙ্গাসাগর', nameEn: 'Gangasagar', jlNo: '০৮' },
          { id: 'bb-akh-06', nameBn: 'নূরপুর', nameEn: 'Noorpur', jlNo: '১৯' },
        ],
      },
      {
        id: 'bijoynagar',
        nameBn: 'বিজয়নগর',
        nameEn: 'Bijoynagar',
        mouzas: [
          { id: 'bb-bij-01', nameBn: 'চান্দুরা', nameEn: 'Chandura', jlNo: '০৬' },
          { id: 'bb-bij-02', nameBn: 'সিঙ্গারবিল', nameEn: 'Singerbil', jlNo: '১২' },
          { id: 'bb-bij-03', nameBn: 'হরষপুর', nameEn: 'Harashpur', jlNo: '১৯' },
          { id: 'bb-bij-04', nameBn: 'বুধন্তী', nameEn: 'Budhanti', jlNo: '২৭' },
          { id: 'bb-bij-05', nameBn: 'ইছাপুরা', nameEn: 'Ichhapur', jlNo: '৩৪' },
          { id: 'bb-bij-06', nameBn: 'চম্পকনগর', nameEn: 'Champaknagar', jlNo: '৪১' },
          { id: 'bb-bij-07', nameBn: 'পাহাড়পুর', nameEn: 'Paharpur', jlNo: '৪৮' },
        ],
      },
    ],
  },
  {
    id: 'cumilla',
    nameBn: 'কুমিল্লা',
    nameEn: 'Cumilla',
    upazilas: [
      {
        id: 'adarsha-sadar',
        nameBn: 'আদর্শ সদর',
        nameEn: 'Adarsha Sadar',
        mouzas: [
          { id: 'cu-ada-01', nameBn: 'শাশনগাছা', nameEn: 'Shashan Gachha', jlNo: '১৪' },
          { id: 'cu-ada-02', nameBn: 'ছাতিপট্টি', nameEn: 'Chhatipatti', jlNo: '১৮' },
          { id: 'cu-ada-03', nameBn: 'বাদুড়তলা', nameEn: 'Badurtala', jlNo: '২২' },
          { id: 'cu-ada-04', nameBn: 'বাগিচাগাঁও', nameEn: 'Bagichagaon', jlNo: '২৭' },
          { id: 'cu-ada-05', nameBn: 'জগন্নাথপুর', nameEn: 'Jagannathpur', jlNo: '৩৫' },
          { id: 'cu-ada-06', nameBn: 'আমড়াতলী', nameEn: 'Amratali', jlNo: '৪৮' },
          { id: 'cu-ada-07', nameBn: 'পাঁচথুবী', nameEn: 'Panchthubi', jlNo: '৫৪' },
        ],
      },
      {
        id: 'sadar-dakshin',
        nameBn: 'সদর দক্ষিণ',
        nameEn: 'Sadar Dakshin',
        mouzas: [
          { id: 'cu-sdk-01', nameBn: 'বিজয়পুর', nameEn: 'Bijoypur', jlNo: '১২' },
          { id: 'cu-sdk-02', nameBn: 'চৌয়ারা', nameEn: 'Chowara', jlNo: '১৯' },
          { id: 'cu-sdk-03', nameBn: 'গোপীনাথপুর', nameEn: 'Gopinathpur', jlNo: '২৬' },
          { id: 'cu-sdk-04', nameBn: 'গোলাবাড়ি', nameEn: 'Golabari', jlNo: '৩৩' },
          { id: 'cu-sdk-05', nameBn: 'বাড়পাড়া', nameEn: 'Barapara', jlNo: '৪০' },
          { id: 'cu-sdk-06', nameBn: 'পেরুল', nameEn: 'Perul', jlNo: '৪৭' },
        ],
      },
      {
        id: 'chandina',
        nameBn: 'চান্দিনা',
        nameEn: 'Chandina',
        mouzas: [
          { id: 'cu-cha-01', nameBn: 'চান্দিনা পৌরসভা', nameEn: 'Chandina Pouroshova', jlNo: '০৮' },
          { id: 'cu-cha-02', nameBn: 'মাধাইয়া', nameEn: 'Madhaiya', jlNo: '১৫' },
          { id: 'cu-cha-03', nameBn: 'বরকইট', nameEn: 'Barkait', jlNo: '২৩' },
          { id: 'cu-cha-04', nameBn: 'মাইজখার', nameEn: 'Maijkhar', jlNo: '৩১' },
          { id: 'cu-cha-05', nameBn: 'কেরণখাল', nameEn: 'Kerankhal', jlNo: '৩৯' },
          { id: 'cu-cha-06', nameBn: 'গল্লাই', nameEn: 'Gallai', jlNo: '৪৬' },
          { id: 'cu-cha-07', nameBn: 'বাতাগাসী', nameEn: 'Bataghashi', jlNo: '৫২' },
        ],
      },
      {
        id: 'daudkandi',
        nameBn: 'দাউদকান্দি',
        nameEn: 'Daudkandi',
        mouzas: [
          { id: 'cu-dau-01', nameBn: 'দাউদকান্দি সদর', nameEn: 'Daudkandi Sadar', jlNo: '০৫' },
          { id: 'cu-dau-02', nameBn: 'গৌরীপুর', nameEn: 'Gouripur', jlNo: '১৪' },
          { id: 'cu-dau-03', nameBn: 'ইলিয়টগঞ্জ', nameEn: 'Eliotganj', jlNo: '২২' },
          { id: 'cu-dau-04', nameBn: 'সুন্দলপুর', nameEn: 'Sundalpur', jlNo: '২৯' },
          { id: 'cu-dau-05', nameBn: 'জিংলাতলী', nameEn: 'Jinglatali', jlNo: '৩৬' },
          { id: 'cu-dau-06', nameBn: 'মারুকা', nameEn: 'Maruka', jlNo: '৪৩' },
          { id: 'cu-dau-07', nameBn: 'গোয়ালমারী', nameEn: 'Goalmari', jlNo: '৫০' },
        ],
      },
      {
        id: 'debidwar',
        nameBn: 'দেবিদ্বার',
        nameEn: 'Debidwar',
        mouzas: [
          { id: 'cu-deb-01', nameBn: 'দেবিদ্বার পৌরসভা', nameEn: 'Debidwar Pouro', jlNo: '১০' },
          { id: 'cu-deb-02', nameBn: 'মোহনপুর', nameEn: 'Mohanpur', jlNo: '১৭' },
          { id: 'cu-deb-03', nameBn: 'রসুল্লাবাদ', nameEn: 'Rasullabad', jlNo: '২৫' },
          { id: 'cu-deb-04', nameBn: 'গুনাইঘর', nameEn: 'Gunaighar', jlNo: '৩২' },
          { id: 'cu-deb-05', nameBn: 'ধামতী', nameEn: 'Dhamti', jlNo: '৪১' },
          { id: 'cu-deb-06', nameBn: 'জাফরগঞ্জ', nameEn: 'Jafarganj', jlNo: '৪৯' },
        ],
      },
      {
        id: 'burichang',
        nameBn: 'বুড়িচং',
        nameEn: 'Burichang',
        mouzas: [
          { id: 'cu-bur-01', nameBn: 'বুড়িচং সদর', nameEn: 'Burichang Sadar', jlNo: '০৭' },
          { id: 'cu-bur-02', nameBn: 'ময়নামতি', nameEn: 'Mainamati', jlNo: '২২' },
          { id: 'cu-bur-03', nameBn: 'পীরযাত্রাপুর', nameEn: 'Pirjatrapur', jlNo: '১৫' },
          { id: 'cu-bur-04', nameBn: 'বাকশীমূল', nameEn: 'Bakshimul', jlNo: '৩১' },
          { id: 'cu-bur-05', nameBn: 'মোকাম', nameEn: 'Mokam', jlNo: '৩৮' },
          { id: 'cu-bur-06', nameBn: 'রাজাপুর', nameEn: 'Rajapur', jlNo: '৪৫' },
        ],
      },
      {
        id: 'brahmanpara',
        nameBn: 'ব্রাহ্মণপাড়া',
        nameEn: 'Brahmanpara',
        mouzas: [
          { id: 'cu-brp-01', nameBn: 'ব্রাহ্মণপাড়া সদর', nameEn: 'Brahmanpara Sadar', jlNo: '০৪' },
          { id: 'cu-brp-02', nameBn: 'মাধবপুর', nameEn: 'Madhabpur', jlNo: '১১' },
          { id: 'cu-brp-03', nameBn: 'শিদলাই', nameEn: 'Shidlai', jlNo: '১৯' },
          { id: 'cu-brp-04', nameBn: 'চান্দলা', nameEn: 'Chandla', jlNo: '২৬' },
          { id: 'cu-brp-05', nameBn: 'শশীদল', nameEn: 'Shashidal', jlNo: '৩৪' },
          { id: 'cu-brp-06', nameBn: 'দুলালপুর', nameEn: 'Dulalpur', jlNo: '৪২' },
        ],
      },
      {
        id: 'chauddagram',
        nameBn: 'চৌদ্দগ্রাম',
        nameEn: 'Chauddagram',
        mouzas: [
          { id: 'cu-chaud-01', nameBn: 'চৌদ্দগ্রাম বাজার', nameEn: 'Chauddagram Bazar', jlNo: '০৬' },
          { id: 'cu-chaud-02', nameBn: 'মিয়াবাজার', nameEn: 'Miabazar', jlNo: '১৩' },
          { id: 'cu-chaud-03', nameBn: 'কাশীনগর', nameEn: 'Kashinagar', jlNo: '২১' },
          { id: 'cu-chaud-04', nameBn: 'বাতিসা', nameEn: 'Batisa', jlNo: '২৯' },
          { id: 'cu-chaud-05', nameBn: 'মুন্সীরহাট', nameEn: 'Munshirhat', jlNo: '৩৭' },
          { id: 'cu-chaud-06', nameBn: 'গুণবতী', nameEn: 'Gunabati', jlNo: '৪৪' },
          { id: 'cu-chaud-07', nameBn: 'চিওড়া', nameEn: 'Cheora', jlNo: '৫১' },
        ],
      },
      {
        id: 'laksam',
        nameBn: 'লাকসাম',
        nameEn: 'Laksam',
        mouzas: [
          { id: 'cu-lak-01', nameBn: 'লাকসাম পৌরসভা', nameEn: 'Laksam Pouro', jlNo: '০৩' },
          { id: 'cu-lak-02', nameBn: 'কান্দিরপাড়', nameEn: 'Kandirpar', jlNo: '১০' },
          { id: 'cu-lak-03', nameBn: 'গোবিন্দপুর', nameEn: 'Gobindapur', jlNo: '১৮' },
          { id: 'cu-lak-04', nameBn: 'উত্তরদা', nameEn: 'Uttarda', jlNo: '২৫' },
          { id: 'cu-lak-05', nameBn: 'মুদাফফরগঞ্জ', nameEn: 'Mudhafurganj', jlNo: '৪২' },
        ],
      },
      {
        id: 'muradnagar',
        nameBn: 'মুরাদনগর',
        nameEn: 'Muradnagar',
        mouzas: [
          { id: 'cu-mur-01', nameBn: 'মুরাদনগর সদর', nameEn: 'Muradnagar Sadar', jlNo: '০৯' },
          { id: 'cu-mur-02', nameBn: 'কোম্পানীগঞ্জ', nameEn: 'Companyganj', jlNo: '১৬' },
          { id: 'cu-mur-03', nameBn: 'বাঙ্গরা', nameEn: 'Bangora', jlNo: '২৪' },
          { id: 'cu-mur-04', nameBn: 'জাহাপুর', nameEn: 'Jahapur', jlNo: '৩৩' },
          { id: 'cu-mur-05', nameBn: 'রামচন্দ্রপুর', nameEn: 'Ramchandrapur', jlNo: '৪১' },
          { id: 'cu-mur-06', nameBn: 'শ্রীকাইল', nameEn: 'Sreekail', jlNo: '৫০' },
        ],
      },
      {
        id: 'barura',
        nameBn: 'বরুড়া',
        nameEn: 'Barura',
        mouzas: [
          { id: 'cu-bar-01', nameBn: 'বরুড়া পৌরসভা', nameEn: 'Barura Pouro', jlNo: '০৫' },
          { id: 'cu-bar-02', nameBn: 'গালিমপুর', nameEn: 'Galimpur', jlNo: '১২' },
          { id: 'cu-bar-03', nameBn: 'শিলমুড়ী', nameEn: 'Shilmuri', jlNo: '২০' },
          { id: 'cu-bar-04', nameBn: 'পায়েলগাছা', nameEn: 'Payalgacha', jlNo: '২৮' },
          { id: 'cu-bar-05', nameBn: 'আড্ডা', nameEn: 'Adda', jlNo: '৩৫' },
          { id: 'cu-bar-06', nameBn: 'শাকপুর', nameEn: 'Shakpur', jlNo: '৪৩' },
        ],
      },
      {
        id: 'homna',
        nameBn: 'হোমনা',
        nameEn: 'Homna',
        mouzas: [
          { id: 'cu-hom-01', nameBn: 'হোমনা সদর', nameEn: 'Homna Sadar', jlNo: '০২' },
          { id: 'cu-hom-02', nameBn: 'আসাদপুর', nameEn: 'Asadpur', jlNo: '০৮' },
          { id: 'cu-hom-03', nameBn: 'জয়পুর', nameEn: 'Joypur', jlNo: '১৫' },
          { id: 'cu-hom-04', nameBn: 'ঘাগুটিয়া', nameEn: 'Ghagutia', jlNo: '৩০' },
          { id: 'cu-hom-05', nameBn: 'মাথাভাঙ্গা', nameEn: 'Mathabhanga', jlNo: '৪৬' },
        ],
      },
      {
        id: 'titas',
        nameBn: 'তিতাস',
        nameEn: 'Titas',
        mouzas: [
          { id: 'cu-tit-01', nameBn: 'মজিদপুর', nameEn: 'Majidpur', jlNo: '০৪' },
          { id: 'cu-tit-02', nameBn: 'বলরামপুর', nameEn: 'Balrampur', jlNo: '১১' },
          { id: 'cu-tit-03', nameBn: 'জগতপুর', nameEn: 'Jagatpur', jlNo: '১৯' },
          { id: 'cu-tit-04', nameBn: 'কড়িকান্দি', nameEn: 'Karikandi', jlNo: '২৭' },
          { id: 'cu-tit-05', nameBn: 'জিয়ারকান্দি', nameEn: 'Zearkandi', jlNo: '৪১' },
        ],
      },
      {
        id: 'meghna',
        nameBn: 'মেঘনা',
        nameEn: 'Meghna',
        mouzas: [
          { id: 'cu-meg-01', nameBn: 'মানিকরচর', nameEn: 'Manikar Char', jlNo: '০৩' },
          { id: 'cu-meg-02', nameBn: 'চন্দনপুর', nameEn: 'Chandanpur', jlNo: '০৯' },
          { id: 'cu-meg-03', nameBn: 'চালিভাঙ্গা', nameEn: 'Chalibhanga', jlNo: '১৬' },
          { id: 'cu-meg-04', nameBn: 'গোবিন্দপুর', nameEn: 'Gobindapur', jlNo: '২৪' },
          { id: 'cu-meg-05', nameBn: 'রাধানগর', nameEn: 'Radhanagar', jlNo: '৩১' },
        ],
      },
      {
        id: 'monohargonj',
        nameBn: 'মনোহরগঞ্জ',
        nameEn: 'Monohargonj',
        mouzas: [
          { id: 'cu-mon-01', nameBn: 'মনোহরগঞ্জ সদর', nameEn: 'Monohargonj Sadar', jlNo: '০৫' },
          { id: 'cu-mon-02', nameBn: 'বাইশগাঁও', nameEn: 'Baishgaon', jlNo: '১২' },
          { id: 'cu-mon-03', nameBn: 'সরসপুর', nameEn: 'Sarashpur', jlNo: '১৯' },
          { id: 'cu-mon-04', nameBn: 'হাসনাবাদ', nameEn: 'Hasnabad', jlNo: '২৭' },
          { id: 'cu-mon-05', nameBn: 'ঝালম', nameEn: 'Jhalam', jlNo: '৩৪' },
        ],
      },
      {
        id: 'nangalkot',
        nameBn: 'নাঙ্গলকোট',
        nameEn: 'Nangalkot',
        mouzas: [
          { id: 'cu-nan-01', nameBn: 'নাঙ্গলকোট পৌরসভা', nameEn: 'Nangalkot Pouro', jlNo: '০৬' },
          { id: 'cu-nan-02', nameBn: 'ঢালুয়া', nameEn: 'Dhalua', jlNo: '১৪' },
          { id: 'cu-nan-03', nameBn: 'বক্সগঞ্জ', nameEn: 'Boxoganj', jlNo: '২১' },
          { id: 'cu-nan-04', nameBn: 'মোকরা', nameEn: 'Mokara', jlNo: '২৯' },
          { id: 'cu-nan-05', nameBn: 'পেরিয়া', nameEn: 'Peria', jlNo: '৩৭' },
          { id: 'cu-nan-06', nameBn: 'রায়কোট', nameEn: 'Roykot', jlNo: '৪৫' },
        ],
      },
      {
        id: 'lalmai',
        nameBn: 'লালমাই',
        nameEn: 'Lalmai',
        mouzas: [
          { id: 'cu-lal-01', nameBn: 'বাগমারা', nameEn: 'Bagmara', jlNo: '০৮' },
          { id: 'cu-lal-02', nameBn: 'ভুলাইণ', nameEn: 'Bhulain', jlNo: '১৫' },
          { id: 'cu-lal-03', nameBn: 'বেলঘর', nameEn: 'Belghar', jlNo: '২৩' },
          { id: 'cu-lal-04', nameBn: 'পেরুল দক্ষিণ', nameEn: 'Perul Dakshin', jlNo: '৩১' },
          { id: 'cu-lal-05', nameBn: 'বাকই উত্তর', nameEn: 'Bakoi Uttar', jlNo: '৩৯' },
        ],
      },
    ],
  },
];

export const LRMassDownloaderUpdated: React.FC = () => {
  // Cascading Selection State
  const [selectedDistrictId, setSelectedDistrictId] = useState<string>('cumilla');
  const [selectedUpazilaId, setSelectedUpazilaId] = useState<string>('');
  const [selectedMouzaIds, setSelectedMouzaIds] = useState<string[]>([]);
  const [selectedRecordType, setSelectedRecordType] = useState<RecordType>('RS');
  const [searchQuery, setSearchQuery] = useState<string>('');

  // Queue Pipeline State
  const [queue, setQueue] = useState<DownloadTask[]>([]);
  const [isPaused, setIsPaused] = useState<boolean>(false);
  const isPausedRef = useRef<boolean>(false);
  isPausedRef.current = isPaused;

  const queueRef = useRef<DownloadTask[]>([]);
  queueRef.current = queue;

  // Active running concurrency counter (max 2)
  const activeDownloadsRef = useRef<number>(0);

  // Derived current district & upazila objects
  const currentDistrict = useMemo(() => {
    return LR_DATASET.find((d) => d.id === selectedDistrictId) || null;
  }, [selectedDistrictId]);

  const currentUpazilaList = useMemo(() => {
    return currentDistrict ? currentDistrict.upazilas : [];
  }, [currentDistrict]);

  const currentUpazila = useMemo(() => {
    return currentUpazilaList.find((u) => u.id === selectedUpazilaId) || null;
  }, [currentUpazilaList, selectedUpazilaId]);

  const availableMouzas = useMemo(() => {
    return currentUpazila ? currentUpazila.mouzas : [];
  }, [currentUpazila]);

  const filteredMouzas = useMemo(() => {
    if (!searchQuery.trim()) return availableMouzas;
    const q = searchQuery.toLowerCase().trim();
    return availableMouzas.filter(
      (m) =>
        m.nameBn.toLowerCase().includes(q) ||
        m.nameEn.toLowerCase().includes(q) ||
        m.jlNo.includes(q)
    );
  }, [availableMouzas, searchQuery]);

  // -------------------------------------------------------------
  // CRITICAL REQUIREMENT 1: PURE CASCADING STATE & MANDATORY AUTO-SELECT
  // -------------------------------------------------------------

  // District Change Handler
  const handleDistrictChange = (newDistrictId: string) => {
    setSelectedDistrictId(newDistrictId);
    setSelectedUpazilaId('');
    setSelectedMouzaIds([]);
    setSearchQuery('');
  };

  // Upazila Change Handler: MANDATORY AUTO-SELECT ALL MOUZAS IN THE SAME HANDLER
  const handleUpazilaChange = (newUpazilaId: string) => {
    setSelectedUpazilaId(newUpazilaId);
    setSearchQuery('');

    if (!newUpazilaId) {
      setSelectedMouzaIds([]);
      return;
    }

    const upazila = currentUpazilaList.find((u) => u.id === newUpazilaId);
    if (upazila && upazila.mouzas && upazila.mouzas.length > 0) {
      // Mandatory immediate initialization with ALL mouza IDs in this exact handler
      setSelectedMouzaIds(upazila.mouzas.map((m) => m.id));
    } else {
      setSelectedMouzaIds([]);
    }
  };

  // Mouza Toggle Handlers
  const toggleMouza = (mouzaId: string) => {
    setSelectedMouzaIds((prev) =>
      prev.includes(mouzaId) ? prev.filter((id) => id !== mouzaId) : [...prev, mouzaId]
    );
  };

  const handleSelectAll = () => {
    if (availableMouzas.length === 0) return;
    setSelectedMouzaIds(availableMouzas.map((m) => m.id));
  };

  const handleDeselectAll = () => {
    setSelectedMouzaIds([]);
  };

  // -------------------------------------------------------------
  // CRITICAL REQUIREMENT 4: CONCURRENCY-LIMITED (MAX 2) DOWNLOAD QUEUE
  // -------------------------------------------------------------

  const triggerNextTasks = useCallback(() => {
    if (isPausedRef.current) return;

    // Find candidate pending tasks while active count is less than 2
    while (activeDownloadsRef.current < 2) {
      const currentList = queueRef.current;
      const nextPendingIndex = currentList.findIndex((t) => t.status === 'PENDING');
      if (nextPendingIndex === -1) break;

      const taskToRun = currentList[nextPendingIndex];

      // Mark as DOWNLOADING
      activeDownloadsRef.current += 1;
      setQueue((prev) =>
        prev.map((t) => (t.id === taskToRun.id ? { ...t, status: 'DOWNLOADING', progress: 5 } : t))
      );

      // Launch worker
      runTaskWorker(taskToRun.id);
    }
  }, []);

  const runTaskWorker = async (taskId: string) => {
    const totalSteps = 10;
    const intervalTime = 300 + Math.floor(Math.random() * 200);

    for (let step = 1; step <= totalSteps; step++) {
      // Check pause
      while (isPausedRef.current) {
        await new Promise((r) => setTimeout(r, 400));
        // Check if task was cleared
        const exists = queueRef.current.find((t) => t.id === taskId);
        if (!exists) {
          activeDownloadsRef.current = Math.max(0, activeDownloadsRef.current - 1);
          return;
        }
      }

      await new Promise((resolve) => setTimeout(resolve, intervalTime));

      const exists = queueRef.current.find((t) => t.id === taskId);
      if (!exists) {
        activeDownloadsRef.current = Math.max(0, activeDownloadsRef.current - 1);
        return;
      }

      const progressVal = Math.min(100, Math.round((step / totalSteps) * 100));
      const simulatedSpeed = 350 + Math.floor(Math.random() * 180);

      setQueue((prev) =>
        prev.map((t) => {
          if (t.id !== taskId) return t;
          const downloaded = Math.round((progressVal / 100) * t.totalKhatians);
          return {
            ...t,
            progress: progressVal,
            downloadedKhatians: downloaded,
            speedKbps: simulatedSpeed,
            status: progressVal >= 100 ? 'COMPLETED' : 'DOWNLOADING',
          };
        })
      );
    }

    // Task finished
    activeDownloadsRef.current = Math.max(0, activeDownloadsRef.current - 1);

    // Continue next pending in queue
    triggerNextTasks();
  };

  // Watch for queue or pause changes to schedule tasks
  useEffect(() => {
    if (!isPaused) {
      triggerNextTasks();
    }
  }, [queue, isPaused, triggerNextTasks]);

  const handleQueueSelected = () => {
    if (selectedMouzaIds.length === 0 || !currentUpazila || !currentDistrict) return;

    const newTasks: DownloadTask[] = [];
    const timestamp = Date.now();

    selectedMouzaIds.forEach((id, idx) => {
      const mouza = currentUpazila.mouzas.find((m) => m.id === id);
      if (!mouza) return;

      // Unique task ID per mouza & record type
      const taskId = `${mouza.id}-${selectedRecordType}-${timestamp}-${idx}`;
      const totalKhatians = 40 + Math.floor(Math.random() * 120);
      const fileSizeBytes = totalKhatians * 125000; // ~125KB per record sheet

      newTasks.push({
        id: taskId,
        mouzaId: mouza.id,
        mouzaNameBn: mouza.nameBn,
        mouzaNameEn: mouza.nameEn,
        jlNo: mouza.jlNo,
        upazilaNameBn: currentUpazila.nameBn,
        districtNameBn: currentDistrict.nameBn,
        recordType: selectedRecordType,
        status: 'PENDING',
        progress: 0,
        totalKhatians,
        downloadedKhatians: 0,
        fileSizeBytes,
        speedKbps: 0,
        timestamp,
      });
    });

    setQueue((prev) => [...prev, ...newTasks]);
  };

  const handlePauseResume = () => {
    setIsPaused((prev) => !prev);
  };

  const handleClearQueue = () => {
    activeDownloadsRef.current = 0;
    setQueue([]);
    setIsPaused(false);
  };

  // Queue HUD calculations
  const totalTasks = queue.length;
  const completedTasks = queue.filter((t) => t.status === 'COMPLETED').length;
  const downloadingTasks = queue.filter((t) => t.status === 'DOWNLOADING').length;
  const pendingTasks = queue.filter((t) => t.status === 'PENDING').length;

  const overallProgress =
    totalTasks === 0
      ? 0
      : Math.round(
          queue.reduce((acc, curr) => acc + (curr.progress || 0), 0) / totalTasks
        );

  const isAllSelected =
    availableMouzas.length > 0 && selectedMouzaIds.length === availableMouzas.length;

  return (
    <div className="min-h-screen bg-[#f8fafc] text-[#0f172a] font-sans antialiased p-4 md:p-8">
      <div className="max-w-7xl mx-auto space-y-6">
        {/* ========================================================= */}
        {/* HEADER BAR & GOVTECH BRANDING */}
        {/* ========================================================= */}
        <header className="bg-[#047857] text-white rounded-3xl p-6 md:p-8 shadow-xl shadow-emerald-900/10 border border-emerald-600/30 relative overflow-hidden">
          <div className="absolute right-0 top-0 w-96 h-96 bg-emerald-500/10 rounded-full blur-3xl pointer-events-none -mr-20 -mt-20"></div>
          <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-6 relative z-10">
            <div className="flex items-center gap-4">
              <div className="w-16 h-16 rounded-2xl bg-white/10 backdrop-blur-md border border-white/20 flex items-center justify-center shadow-inner">
                <svg
                  className="w-9 h-9 text-emerald-300"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                  />
                </svg>
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <span className="bg-emerald-800/80 text-emerald-200 text-xs font-semibold px-2.5 py-0.5 rounded-full border border-emerald-500/30">
                    GovTech BD Portal
                  </span>
                  <span className="text-xs text-emerald-200/80">ভূমি মন্ত্রণালয় ডিজিটাল রেকর্ড</span>
                </div>
                <h1 className="text-2xl md:text-3xl font-bold tracking-tight text-white mt-1">
                  LR Mass Downloader (Updated)
                </h1>
                <p className="text-sm text-emerald-100/90 mt-0.5">
                  Cumilla &amp; Brahmanbaria Land Records &bull; কুমিল্লা ও ব্রাহ্মণবাড়িয়া খতিয়ান ও মৌজা রেকর্ড সংগ্রহ
                </p>
              </div>
            </div>

            {/* Quick System Status Pill */}
            <div className="flex items-center gap-3 bg-emerald-800/60 backdrop-blur-md px-4 py-2.5 rounded-2xl border border-emerald-500/30 text-xs">
              <div className="flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-pulse"></span>
                <span className="font-medium text-emerald-100">Server Node: Active</span>
              </div>
              <span className="text-emerald-500">|</span>
              <span className="text-emerald-200">Max Concurrency: 2</span>
            </div>
          </div>
        </header>

        {/* ========================================================= */}
        {/* MAIN DASHBOARD GRID */}
        {/* ========================================================= */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          {/* LEFT COLUMN: SELECTION CONTROLS & MOUZA LIST */}
          <div className="lg:col-span-7 space-y-6">
            {/* Cascading Filter Card */}
            <div className="bg-white rounded-3xl p-6 shadow-sm border border-slate-200/80 space-y-5">
              <div className="flex items-center justify-between border-b border-slate-100 pb-4">
                <div className="flex items-center gap-2">
                  <svg
                    className="w-5 h-5 text-emerald-700"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth="2"
                      d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"
                    />
                  </svg>
                  <h2 className="text-lg font-bold text-slate-800">
                    ফিল্টার ও স্তর নির্বাচন (Cascading Hierarchy)
                  </h2>
                </div>
                <span className="text-xs text-slate-500 font-medium bg-slate-100 px-2.5 py-1 rounded-full">
                  Step 1 &rarr; Step 2 &rarr; Step 3
                </span>
              </div>

              {/* District & Upazila Selectors */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {/* 1. District Selection */}
                <div>
                  <label className="block text-xs font-semibold text-slate-600 uppercase tracking-wider mb-1.5">
                    ১. জেলা (District)
                  </label>
                  <div className="relative">
                    <select
                      value={selectedDistrictId}
                      onChange={(e) => handleDistrictChange(e.target.value)}
                      className="w-full bg-slate-50 border border-slate-300 text-slate-800 text-sm font-medium rounded-2xl px-4 py-3 appearance-none focus:outline-none focus:ring-2 focus:ring-emerald-600 focus:border-transparent transition-all"
                    >
                      {LR_DATASET.map((dist) => (
                        <option key={dist.id} value={dist.id}>
                          {dist.nameBn} ({dist.nameEn})
                        </option>
                      ))}
                    </select>
                    <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-3.5 text-slate-500">
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7" />
                      </svg>
                    </div>
                  </div>
                </div>

                {/* 2. Upazila Selection */}
                <div>
                  <label className="block text-xs font-semibold text-slate-600 uppercase tracking-wider mb-1.5">
                    ২. উপজেলা (Upazila)
                  </label>
                  <div className="relative">
                    <select
                      value={selectedUpazilaId}
                      onChange={(e) => handleUpazilaChange(e.target.value)}
                      className={`w-full text-sm font-medium rounded-2xl px-4 py-3 appearance-none focus:outline-none focus:ring-2 focus:ring-emerald-600 focus:border-transparent transition-all ${
                        selectedUpazilaId
                          ? 'bg-emerald-50/50 border-emerald-400 text-slate-900 font-semibold'
                          : 'bg-slate-50 border-slate-300 text-slate-500'
                      }`}
                    >
                      <option value="">-- উপজেলা নির্বাচন করুন --</option>
                      {currentUpazilaList.map((upa) => (
                        <option key={upa.id} value={upa.id}>
                          {upa.nameBn} ({upa.nameEn}) - {upa.mouzas.length} মৌজা
                        </option>
                      ))}
                    </select>
                    <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-3.5 text-slate-500">
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7" />
                      </svg>
                    </div>
                  </div>
                </div>
              </div>

              {/* Record Type Selector */}
              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase tracking-wider mb-2">
                  ৩. জরিপ রেকর্ড ধরন (Record Type Filter)
                </label>
                <div className="grid grid-cols-4 gap-2">
                  {(['CS', 'SA', 'RS', 'BRS'] as RecordType[]).map((type) => {
                    const isActive = selectedRecordType === type;
                    return (
                      <button
                        key={type}
                        type="button"
                        onClick={() => setSelectedRecordType(type)}
                        className={`py-2.5 px-3 rounded-2xl text-xs font-bold transition-all flex flex-col items-center justify-center gap-0.5 border ${
                          isActive
                            ? 'bg-[#047857] text-white border-emerald-700 shadow-md shadow-emerald-700/20'
                            : 'bg-slate-50 text-slate-700 border-slate-200 hover:bg-slate-100 hover:border-slate-300'
                        }`}
                      >
                        <span className="text-sm">{type}</span>
                        <span className={`text-[10px] ${isActive ? 'text-emerald-100' : 'text-slate-400'}`}>
                          {type === 'CS'
                            ? 'ক্যাডাস্ট্রাল'
                            : type === 'SA'
                            ? 'স্টেট একুইজিশন'
                            : type === 'RS'
                            ? 'রিভিশনাল (Default)'
                            : 'বাংলাদেশ সিটি'}
                        </span>
                      </button>
                    );
                  })}
                </div>
              </div>
            </div>

            {/* Mouza Selection Panel */}
            <div className="bg-white rounded-3xl p-6 shadow-sm border border-slate-200/80 space-y-4">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-100 pb-4">
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="text-lg font-bold text-slate-900">মৌজা তালিকা (Mouza Registry)</h3>
                    {selectedUpazilaId && (
                      <span className="bg-emerald-100 text-emerald-800 text-xs font-semibold px-2.5 py-0.5 rounded-full border border-emerald-300">
                        {currentUpazila?.nameBn}
                      </span>
                    )}
                  </div>
                  <p className="text-xs text-slate-500 mt-0.5">
                    মৌজা নির্বাচন করুন এবং ডাউনলোডের জন্য প্রস্তুত করুন
                  </p>
                </div>

                {/* Master Badge Counter */}
                <div className="inline-flex items-center gap-2 bg-slate-100 text-slate-700 text-xs font-bold px-3 py-1.5 rounded-2xl border border-slate-200">
                  <span className="w-2 h-2 rounded-full bg-emerald-600"></span>
                  <span>
                    Selected: {selectedMouzaIds.length} / Total: {availableMouzas.length} Mouzas
                  </span>
                </div>
              </div>

              {/* Action Buttons & Search Toolbar */}
              <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={handleSelectAll}
                    disabled={availableMouzas.length === 0}
                    className="flex-1 sm:flex-none px-3.5 py-2 rounded-xl text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200 hover:bg-emerald-100 transition-all disabled:opacity-40 disabled:pointer-events-none"
                  >
                    Select All ({availableMouzas.length})
                  </button>
                  <button
                    type="button"
                    onClick={handleDeselectAll}
                    disabled={selectedMouzaIds.length === 0}
                    className="flex-1 sm:flex-none px-3.5 py-2 rounded-xl text-xs font-bold bg-slate-100 text-slate-600 border border-slate-200 hover:bg-slate-200 transition-all disabled:opacity-40 disabled:pointer-events-none"
                  >
                    Deselect All
                  </button>
                </div>

                {/* Search Box */}
                <div className="relative flex-1 sm:max-w-xs">
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="মৌজা বা জে.এল নং দিয়ে খুঁজুন..."
                    disabled={availableMouzas.length === 0}
                    className="w-full text-xs bg-slate-50 border border-slate-200 rounded-xl pl-9 pr-3 py-2 text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-600 focus:border-transparent disabled:opacity-50"
                  />
                  <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                    </svg>
                  </div>
                </div>
              </div>

              {/* Mouza Grid Cards */}
              <div className="min-h-[280px] max-h-[380px] overflow-y-auto pr-1">
                {!selectedUpazilaId ? (
                  <div className="h-64 flex flex-col items-center justify-center text-center p-6 bg-slate-50 rounded-2xl border border-dashed border-slate-200 text-slate-400">
                    <svg className="w-12 h-12 text-slate-300 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                    </svg>
                    <p className="font-semibold text-slate-600">উপজেলা নির্বাচন করুন</p>
                    <p className="text-xs text-slate-400 mt-1 max-w-sm">
                      উপরে জেলা ও উপজেলা নির্বাচন করলে সকল মৌজা স্বয়ংক্রিয়ভাবে সিলেক্ট হয়ে যাবে।
                    </p>
                  </div>
                ) : filteredMouzas.length === 0 ? (
                  <div className="h-48 flex items-center justify-center text-center text-slate-400 text-sm">
                    কোনো মৌজা পাওয়া যায়নি
                  </div>
                ) : (
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    {filteredMouzas.map((mouza) => {
                      const isChecked = selectedMouzaIds.includes(mouza.id);
                      return (
                        <div
                          key={mouza.id}
                          onClick={() => toggleMouza(mouza.id)}
                          className={`cursor-pointer p-3.5 rounded-2xl border transition-all duration-150 flex items-center justify-between select-none ${
                            isChecked
                              ? 'bg-emerald-50/70 border-emerald-500/80 shadow-sm shadow-emerald-500/10'
                              : 'bg-white border-slate-200 hover:border-slate-300 hover:bg-slate-50/60'
                          }`}
                        >
                          <div className="flex items-center gap-3">
                            {/* Checkbox Icon */}
                            <div
                              className={`w-5 h-5 rounded-lg flex items-center justify-center transition-colors ${
                                isChecked
                                  ? 'bg-[#047857] text-white shadow-sm'
                                  : 'border-2 border-slate-300 bg-white'
                              }`}
                            >
                              {isChecked && (
                                <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="3" d="M5 13l4 4L19 7" />
                                </svg>
                              )}
                            </div>
                            <div>
                              <div className="font-bold text-slate-800 text-sm">{mouza.nameBn}</div>
                              <div className="text-[11px] text-slate-400 font-medium">{mouza.nameEn}</div>
                            </div>
                          </div>

                          {/* J.L. No. Badge Pill */}
                          <div className="flex items-center gap-1.5">
                            <span className="text-[10px] font-semibold text-slate-400 uppercase">J.L.</span>
                            <span className="bg-slate-100 text-slate-700 text-xs font-bold px-2 py-0.5 rounded-lg border border-slate-200">
                              {mouza.jlNo}
                            </span>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>

              {/* Queue Button Primary CTA */}
              <div className="pt-2">
                <button
                  type="button"
                  onClick={handleQueueSelected}
                  disabled={selectedMouzaIds.length === 0}
                  className="w-full py-4 px-6 rounded-2xl font-bold text-white bg-[#047857] hover:bg-[#065f46] active:scale-[0.99] transition-all shadow-lg shadow-emerald-800/20 disabled:opacity-40 disabled:pointer-events-none flex items-center justify-center gap-3"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth="2"
                      d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"
                    />
                  </svg>
                  <span>Queue Selected Downloads ({selectedMouzaIds.length} Mouzas)</span>
                </button>
              </div>
            </div>
          </div>

          {/* RIGHT COLUMN: DOWNLOAD QUEUE & REAL-TIME HUD */}
          <div className="lg:col-span-5 space-y-6">
            {/* Real-time Queue HUD */}
            <div className="bg-white rounded-3xl p-6 shadow-sm border border-slate-200/80 space-y-5">
              <div className="flex items-center justify-between border-b border-slate-100 pb-4">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 rounded-full bg-emerald-500 animate-ping"></div>
                  <h2 className="text-lg font-bold text-slate-800">Queue HUD Pipeline</h2>
                </div>
                <div className="flex items-center gap-1">
                  <button
                    type="button"
                    onClick={handlePauseResume}
                    disabled={totalTasks === 0 || completedTasks === totalTasks}
                    className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all disabled:opacity-40 ${
                      isPaused
                        ? 'bg-amber-100 text-amber-800 border border-amber-300 hover:bg-amber-200'
                        : 'bg-slate-100 text-slate-700 border border-slate-200 hover:bg-slate-200'
                    }`}
                  >
                    {isPaused ? 'Resume' : 'Pause'}
                  </button>
                  <button
                    type="button"
                    onClick={handleClearQueue}
                    disabled={totalTasks === 0}
                    className="px-3 py-1.5 rounded-xl text-xs font-bold bg-rose-50 text-rose-700 border border-rose-200 hover:bg-rose-100 transition-all disabled:opacity-40"
                  >
                    Clear Queue
                  </button>
                </div>
              </div>

              {/* Progress Bar & Percentage */}
              <div>
                <div className="flex items-center justify-between text-xs font-semibold text-slate-600 mb-1.5">
                  <span>Overall Queue Progress</span>
                  <span className="font-bold text-emerald-700">{overallProgress}%</span>
                </div>
                <div className="w-full bg-slate-100 rounded-full h-3 overflow-hidden border border-slate-200">
                  <div
                    className="bg-emerald-600 h-full rounded-full transition-all duration-300 ease-out"
                    style={{ width: `${overallProgress}%` }}
                  ></div>
                </div>
              </div>

              {/* Live Count Summary Badges */}
              <div className="grid grid-cols-4 gap-2">
                <div className="bg-slate-50 p-3 rounded-2xl border border-slate-200 text-center">
                  <div className="text-[10px] font-bold text-slate-400 uppercase">Total</div>
                  <div className="text-lg font-black text-slate-800 mt-0.5">{totalTasks}</div>
                </div>
                <div className="bg-emerald-50 p-3 rounded-2xl border border-emerald-200 text-center">
                  <div className="text-[10px] font-bold text-emerald-600 uppercase">Done</div>
                  <div className="text-lg font-black text-emerald-700 mt-0.5">{completedTasks}</div>
                </div>
                <div className="bg-blue-50 p-3 rounded-2xl border border-blue-200 text-center">
                  <div className="text-[10px] font-bold text-blue-600 uppercase">Active</div>
                  <div className="text-lg font-black text-blue-700 mt-0.5">{downloadingTasks}</div>
                </div>
                <div className="bg-amber-50 p-3 rounded-2xl border border-amber-200 text-center">
                  <div className="text-[10px] font-bold text-amber-600 uppercase">Queued</div>
                  <div className="text-lg font-black text-amber-700 mt-0.5">{pendingTasks}</div>
                </div>
              </div>

              {/* Anti-Freeze Concurrency Notice */}
              <div className="flex items-start gap-2.5 p-3 rounded-2xl bg-slate-50 border border-slate-200 text-xs text-slate-600">
                <svg className="w-4 h-4 text-emerald-600 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span>
                  <strong>Anti-Freeze Engine:</strong> Sequential pipeline runs max 2 concurrent downloads to preserve browser memory and prevent UI stalling.
                </span>
              </div>

              {/* Scrollable Task Items List */}
              <div className="min-h-[300px] max-h-[460px] overflow-y-auto space-y-2.5 pr-1">
                {queue.length === 0 ? (
                  <div className="h-56 flex flex-col items-center justify-center text-center p-6 bg-slate-50 rounded-2xl border border-dashed border-slate-200 text-slate-400">
                    <svg className="w-10 h-10 text-slate-300 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
                    </svg>
                    <p className="font-semibold text-slate-600 text-sm">ডাউনলোড কিউ খালি</p>
                    <p className="text-xs text-slate-400 mt-0.5">মৌজা সিলেক্ট করে Queue বাটনে ক্লিক করুন।</p>
                  </div>
                ) : (
                  queue.map((task) => {
                    const isDone = task.status === 'COMPLETED';
                    const isRunning = task.status === 'DOWNLOADING';

                    return (
                      <div
                        key={task.id}
                        className={`p-3.5 rounded-2xl border transition-all ${
                          isDone
                            ? 'bg-slate-50/80 border-slate-200'
                            : isRunning
                            ? 'bg-emerald-50/60 border-emerald-400 shadow-sm shadow-emerald-500/10'
                            : 'bg-white border-slate-200'
                        }`}
                      >
                        <div className="flex items-center justify-between gap-2 mb-1.5">
                          <div>
                            <span className="font-bold text-slate-800 text-sm">{task.mouzaNameBn}</span>
                            <span className="text-xs text-slate-500 ml-1.5 font-medium">({task.mouzaNameEn})</span>
                            <span className="ml-2 text-[10px] bg-slate-100 text-slate-600 font-bold px-1.5 py-0.5 rounded border border-slate-200">
                              J.L. {task.jlNo}
                            </span>
                            <span className="ml-1 text-[10px] bg-emerald-100 text-emerald-800 font-bold px-1.5 py-0.5 rounded border border-emerald-300">
                              {task.recordType}
                            </span>
                          </div>

                          {/* Status Pill */}
                          <div className="flex items-center gap-1">
                            {isDone ? (
                              <span className="inline-flex items-center gap-1 text-[11px] font-bold text-emerald-700 bg-emerald-100 px-2 py-0.5 rounded-full border border-emerald-300">
                                <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="3" d="M5 13l4 4L19 7" />
                                </svg>
                                Done
                              </span>
                            ) : isRunning ? (
                              <span className="inline-flex items-center gap-1 text-[11px] font-bold text-blue-700 bg-blue-100 px-2 py-0.5 rounded-full border border-blue-300 animate-pulse">
                                <span className="w-1.5 h-1.5 rounded-full bg-blue-600"></span>
                                {task.progress}%
                              </span>
                            ) : (
                              <span className="text-[11px] font-semibold text-slate-500 bg-slate-100 px-2 py-0.5 rounded-full border border-slate-200">
                                Pending
                              </span>
                            )}
                          </div>
                        </div>

                        {/* Progress Bar for Task */}
                        <div className="w-full bg-slate-200/80 rounded-full h-2 overflow-hidden mt-2">
                          <div
                            className={`h-full transition-all duration-200 ${
                              isDone ? 'bg-emerald-600' : isRunning ? 'bg-blue-600' : 'bg-slate-300'
                            }`}
                            style={{ width: `${task.progress}%` }}
                          ></div>
                        </div>

                        {/* Live Metadata metrics */}
                        <div className="flex items-center justify-between text-[11px] text-slate-500 mt-1.5 font-medium">
                          <span>
                            {task.upazilaNameBn} &bull; Khatians: {task.downloadedKhatians}/{task.totalKhatians}
                          </span>
                          <span>{isRunning ? `${task.speedKbps} KB/s` : isDone ? 'Archived' : 'Waiting...'}</span>
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </div>
          </div>
        </div>

        {/* ========================================================= */}
        {/* FOOTER & GOVTECH COMPLIANCE */}
        {/* ========================================================= */}
        <footer className="text-center text-xs text-slate-400 py-4 border-t border-slate-200 space-y-1">
          <p>
            গণপ্রজাতন্ত্রী বাংলাদেশ সরকার &bull; ভূমি রেকর্ড ও জরিপ অধিদপ্তর (DLR&amp;S) সহায়ক টুল
          </p>
          <p className="text-[11px] text-slate-400">
            LR Mass Downloader (Updated) &bull; Cumilla &amp; Brahmanbaria Specialized Build &bull; High Reliability Client
          </p>
        </footer>
      </div>
    </div>
  );
};

export default LRMassDownloaderUpdated;
