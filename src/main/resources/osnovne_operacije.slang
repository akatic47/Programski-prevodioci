zapocni_igru
moj broj original;
moj broj kljuc;
moj broj sifrovano;
moj broj dekriptovano;

napisi("Unesi broj za sifrovanje:");
upisi(original);
napisi("Unesi kljuc (1-9):");
upisi(kljuc);

ako(kljuc < 1 ili kljuc > 9){
    napisi("Greska: Kljuc mora biti izmedju 1 i 9!");
}
inace{
    sifrovano = enkriptuj(original, kljuc);
    napisi("Sifrovano:");
    napisi(sifrovano);

    dekriptovano = dekriptuj(sifrovano, kljuc);
    napisi("Desifrovano: je");
    napisi(dekriptovano);

}

zavrsi_igru;