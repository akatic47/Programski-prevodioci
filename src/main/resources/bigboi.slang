broj f1(broj x, broj y) {
    vrati x + y;
}

realan f2(realan a, realan b) {
    vrati a * b - 3.14;
}

slovo prviChar(tekst s) {
    moj broj nn;
    nn = 0;
    vrati 'A';
}

tekst concat2(tekst a, tekst b) {
    vrati a;
}

pogodak jePozitivno(broj x) {
    ako (x > 0) {
        vrati tacno;
    } inace {
        vrati netacno;
    }
}

niz napravi1D(broj n) {
    moj niz arr[n];
    moj broj rr;
    rr = 0;

    radi(rr < n) {
        arr[rr] = rr * 2;
        rr = rr + 1;
    }

    vrati arr;
}

niz napravi2D(broj r, broj c) {
    moj niz mat[r][c];
    moj broj rr;
    moj broj cc;

    rr = 0;
    radi(rr < r) {
        cc = 0;
        radi(cc < c) {
            mat[rr][cc] = rr + cc;
            cc = cc + 1;
        }
        rr = rr + 1;
    }

    vrati mat;
}

broj suma2D(niz m, broj r, broj c) {
    moj broj rr;
    moj broj cc;
    moj broj s;

    s = 0;
    rr = 0;

    radi(rr < r) {
        cc = 0;
        radi(cc < c) {
            s = s + m[rr][cc];
            cc = cc + 1;
        }
        rr = rr + 1;
    }

    vrati s;
}

niz transponuj(niz a, broj rows, broj cols) {
    moj niz t[cols][rows];
    moj broj rr;
    moj broj cc;

    rr = 0;
    radi(rr < rows) {
        cc = 0;
        radi(cc < cols) {
            t[cc][rr] = a[rr][cc];
            cc = cc + 1;
        }
        rr = rr + 1;
    }

    vrati t;
}

broj absval(broj x) {
    ako(x < 0) {
        vrati 0 - x;
    } inace {
        vrati x;
    }
}

broj testLogika(broj a, broj b) {
    ako(a < b i b < 10) {
        vrati 1;
    } inace {
        vrati 0;
    }
}

broj testRelacije(broj a, broj b) {
    ako(a != b ili a >= b) {
        vrati 777;
    } inace {
        vrati 0;
    }
}

tekst identitet(tekst x) {
    vrati x;
}

moj broj globalA;
moj realan globalB;
moj tekst globalS;
moje slovo globalC;

zapocni_igru
    moj broj xx;
    moj realan yy;
    moj tekst msg;
    moj slovo ch;
    moj niz arr[5];
    moj niz mat[2][3];

    xx = 5;
    yy = 2.5;
    msg = "Hello";
    ch = 'Z';

    arr[0] = 10;
    arr[1] = 20;
    arr[2] = 30;
    arr[3] = 40;
    arr[4] = 50;

    mat[0][0] = 1;
    mat[0][1] = 2;
    mat[0][2] = 3;

    mat[1][0] = 4;
    mat[1][1] = 5;
    mat[1][2] = 6;

    xx = f1(3, 7);
    yy = f2(1.5, 2.5);

    msg = concat2("A", "B");
    ch = prviChar("Slovo");

    globalA = absval(-99);

    moj broj s2d;
    s2d = suma2D(mat, 2, 3);

    moj niz tmat;
    tmat = transponuj(mat, 2, 3);

    napisi(xx);
    napisi(yy);
    napisi(msg);
    napisi(ch);
    napisi(globalA);
    napisi(s2d);
    napisi(tmat[1][0]);
zavrsi_igru;
