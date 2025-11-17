broj prost(broj n){
    ako(n <= 1){
        vrati 0;
    }
    moj broj d = 2;
    radi(d * d <= n){
        ako(n % d == 0){
            vrati 0;
        }
        d = d + 1;
    }
    vrati 1;
}

zapocni_igru
moj broj br = 17;
ako(prost(br)){
    napisi(1);
}
inace{
    napisi(0);
}
zavrsi_igru;