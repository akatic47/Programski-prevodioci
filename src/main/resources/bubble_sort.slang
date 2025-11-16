zapocni_igru
moj niz n[5];
n[0] = 64; n[1] = 34; n[2] = 25; n[3] = 12; n[4] = 90;

moj broj i;
moj broj j;
moj broj temp;

i = 0;
radi(i < 4){
    j = 0;
    radi(j < 4 - i){
        ako(n[j] > n[j+1]){
            temp = n[j];
            n[j] = n[j+1];
            n[j+1] = temp;
        }
        j = j + 1;
    }
    i = i + 1;
}

i = 0;
radi(i < 5){
    napisi(n[i]);
    i = i + 1;
}
zavrsi_igru;