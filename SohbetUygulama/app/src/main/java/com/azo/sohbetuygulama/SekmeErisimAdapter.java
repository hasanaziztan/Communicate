package com.azo.sohbetuygulama;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

// fragmentpageradapter kalıtım aldık
public class SekmeErisimAdapter extends FragmentPagerAdapter {
    public SekmeErisimAdapter(FragmentManager fm) {
        super(fm);
    }

    // Toolbar üzerinde oluşturulan fragmentlere erişim
    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 1:
                GruplarFragment gruplarFragment = new GruplarFragment();
                return gruplarFragment;
            case 2:
                KisilerFragment  kisilerFragment = new KisilerFragment();
                return kisilerFragment;
            case 3:
                TaleplerFragment  taleplerFragment = new TaleplerFragment();
                return taleplerFragment;

                default:
                    return null;
        }
    }
    //Fragment sayısı
    @Override
    public int getCount() {
        return 4;
    }
    // toolbar üzerindeki yazılar
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Sohbetler";
            case 1:
                return "Gruplar";
            case 2:
                return "Kişiler";
            case 3:
                return "Talepler";
            default:
                return null;
        }
    }
}
