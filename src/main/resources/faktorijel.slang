broj faktorijel(broj n){
    moj broj rez = 1;
    moj broj br = 2;
    radi(br <= n){
        rez = rez * br;
        br = br + 1;
    }
    vrati rez;
}

zapocni_igru
moj broj x = 5;
napisi(faktorijel(x));
zavrsi_igru;