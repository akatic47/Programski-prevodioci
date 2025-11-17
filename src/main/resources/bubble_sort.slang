zapocni_igru
moj niz n[5];
n[0] = 64; n[1] = 34; n[2] = 25; n[3] = 12; n[4] = 90;

moj broj a;
moj broj b;
moj broj temp;

a = 0;
radi(a < 4){
    b = 0;
    radi(b < 4 - a){
        ako(n[b] > n[b+1]){
            temp = n[b];
            n[b] = n[b+1];
            n[b+1] = temp;
        }
        b = b + 1;
    }
    a = a + 1;
}

a = 0;
radi(a < 5){
    napisi(n[a]);
    a = a + 1;
}
zavrsi_igru;
