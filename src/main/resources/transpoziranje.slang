 niz transponuj(niz a[][], broj n, broj m) {
    moj niz t[m][n];
    moj broj i;
    moj broj j;

    radi (i < n) {
        radi (j < m) {
            t[j][i] = a[i][j];
            j = j + 1;
        }
        i = i + 1;
    }

    vrati t;
}

zapocni_igru
    moj niz m[2][3];
    moj niz mt[3][2];

    m[0][0] = 1;
    m[0][1] = 2;
    m[0][2] = 3;
    m[1][0] = 4;
    m[1][1] = 5;
    m[1][2] = 6;

    mt = transponuj(m, 2, 3);

    napisi(mt);
zavrsi_igru;
