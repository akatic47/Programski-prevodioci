broj faktorijel(broj n){
    moj broj rez = 1;
    moj broj i = 2;
    radi(i <= n){
        rez = rez * i;
        i = i + 1;
    }
    vrati rez;
}

zapocni_igru
moj broj x = 5;
napisi(faktorijel(x));
zavrsi_igru;