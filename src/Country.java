import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;

public abstract class Country
{
    private final String name;
    // Statyczne, prywatne pola przechowujące ścieżki do plików CSV
    private static String path_cases="confirmed_cases.csv";
    private static String path_deaths="deaths.csv";




    public String getName()
    {
        return name;
    }


    public Country(String name)
    {
        this.name=name;
    }



    //Wewnętrzna klasa CountryColumns;
    private static class CountryColumns
    {
        public final int firstColumnIndex;
        public final int columnCount;

        //konstrukor ustawiający powyższe pola
        public CountryColumns(int firstColumnIndex, int columnCount)
        {
            this.firstColumnIndex=firstColumnIndex;
            this.columnCount=columnCount;
        }


    }

    // Statyczna metoda setFiles, która ustawia ścieżki do plików CSV
    public static void setFiles(String path_cases, String path_deaths) throws FileNotFoundException
    {
        // Sprawdzamy, czy pliki istnieją i są czytelne
        File fileCases = new File(path_cases);
        if(!fileCases.exists() || !fileCases.canRead())
        {
            throw new FileNotFoundException("PLIKU NIE ODNAZEZIONO LUB NIE JEST CZYTELNY"+path_cases);
        }

        File fileDeaths=new File(path_deaths);
        if(!fileDeaths.exists() || !fileDeaths.canRead())
        {
            throw new FileNotFoundException("PLIKU NIE ODNAZEZIONO LUB NIE JEST CZYTELNY"+path_deaths);
        }

        // Jeśli pliki są prawidłowe, ustawiamy je w polach statycznych
        Country.path_cases=path_cases;
        Country.path_deaths=path_cases;
    }


   //metoda fromCsv
    public static Country fromCsv(String countryName) throws IOException, CountryNotFoundException
    {
        // Tworzymy obiekty BufferedReader do odczytu plików CSV
        BufferedReader casesReader = new BufferedReader(new FileReader(path_cases));  // Otwieramy plik z przypadkami
        BufferedReader deathsReader = new BufferedReader(new FileReader(path_deaths));  // Otwieramy plik ze zgonami

        // Odczytujemy pierwszy wiersz z każdego pliku CSV (pierwszy wiersz zawiera nagłówki kolumn)
        String firstRowCases = casesReader.readLine();  // Odczytujemy pierwszy wiersz z pliku z przypadkami
        String firstRowDeaths = deathsReader.readLine();  // Odczytujemy pierwszy wiersz z pliku ze zgonami

        // Sprawdzamy, czy którykolwiek z plików jest pusty (jeśli tak, rzucamy wyjątek)
        if (firstRowCases == null || firstRowDeaths == null)
        {
            throw new IOException("Pliki CSV są puste lub niepoprawne");  // Jeśli plik jest pusty lub niepoprawny, zgłaszamy wyjątek
        }

        // Zamykamy BufferedReader (w try-with-resources pliki są automatycznie zamykane po zakończeniu działania metody)
        casesReader.close();
        deathsReader.close();

        // Używamy metody getCountryColumns, aby sprawdzić, w której kolumnie znajduje się poszukiwane państwo
        // oraz liczbę kolumn, które odpowiadają temu państwu
        CountryColumns countryColumns = getCountryColumns(firstRowCases, countryName);

        // Jeśli państwo nie zostało znalezione w pliku, rzucamy wyjątek CountryNotFoundException
        if (countryColumns == null)
        {
            throw new CountryNotFoundException(countryName);  // Jeśli nie znaleziono państwa, zgłaszamy wyjątek
        }

        // Pobieramy wszystkie daty z nagłówka
        String[] headerParts = firstRowCases.split(",");
        List<LocalDate> dates = new ArrayList<>();

        // Daty zaczynają się od kolumny countryColumns.firstColumnIndex
        for (int i = countryColumns.firstColumnIndex; i < headerParts.length; i++)
        {
            dates.add(LocalDate.parse(headerParts[i]));
        }

        // Jeśli państwo ma tylko jedną kolumnę (bez prowincji), tworzymy obiekt CountryWithoutProvinces
        if (countryColumns.columnCount == 1)
        {
           CountryWithoutProvinces country=new CountryWithoutProvinces(countryName);

           String lineCases;
           String lineDeaths;

           while( (lineCases=casesReader.readLine())!=null && (lineDeaths=deathsReader.readLine())!=null )
           {
               String[] partsCases=lineCases.split(";");
               String[] partsDeaths=lineDeaths.split(";");

               // Sprawdzamy, czy ta linia odpowiada naszemu krajowi
               if(partsCases[0].equalsIgnoreCase(countryName))
               {
                   // Dla każdej daty pobieramy liczbę przypadków i zgonów
                   for(int i=0;i<dates.size();i++)
                   {
                       int index=countryColumns.firstColumnIndex+i;
                       int cases=Integer.parseInt(partsCases[i]);
                       int deaths=Integer.parseInt(partsDeaths[index]);

                       // Dodajemy statystyki do obiektu kraju
                       country.addDailyStatistic(dates.get(i),cases,deaths);
                   }
                   break;  // Znaleziono dane dla kraju — przerywamy dalsze czytanie
               }
           }

           return country;  // Zwracamy obiekt bez prowincji


        }
        else
        {
            // Jeśli państwo ma prowincje, musimy załadować te prowincje
            // Używamy metody loadProvinces, aby pobrać tablicę prowincji
            Country[] provinces = loadProvinces(firstRowCases, countryColumns.firstColumnIndex, countryColumns.columnCount);

            // Tworzymy obiekt kraju z prowincjami
            CountryWithProvinces country= new CountryWithProvinces(countryName, provinces);


            String lineCases, lineDeaths;
            int provinceIndex = 0;


            // Wczytujemy linie z plików (każda linia = jedna prowincja)
            while ((lineCases = casesReader.readLine()) != null && (lineDeaths = deathsReader.readLine()) != null)
            {

                String[] partsCases = lineCases.split(";");
                String[] partsDeaths = lineDeaths.split(";");

                // Sprawdzamy, czy w tej linii jest dane z nazwą kraju (lub jego prowincji)
                if (partsCases[0].equalsIgnoreCase(countryName)) {
                    for (int i = 0; i < dates.size(); i++)
                    {
                        int idx = countryColumns.firstColumnIndex + i;
                        int cases = Integer.parseInt(partsCases[idx]);
                        int deaths =Integer.parseInt(partsDeaths[idx]);


                        // Dodajemy statystyki do odpowiedniej prowincji
                        if (provinceIndex < provinces.length)
                        {
                            if (provinces[provinceIndex] instanceof CountryWithoutProvinces p)
                            {
                                p.addDailyStatistic(dates.get(i), cases, deaths);
                            }
                        }
                    }
                    provinceIndex++;    // Przechodzimy do następnej prowincji
                }
            }
            return country; // Zwracamy obiekt kraju z prowincjami
        }
    }


    private static Country[] loadProvinces(String firstRow, int startColumnIndex, int columnCount)
    {
        // Implementacja ładowania prowincji
        // Przykład: Tworzymy tablicę z prowincjami, w zależności od danych w CSV
        Country[] provinces = new Country[columnCount - 1];  // Zmniejszamy o 1, bo same państwo to 1 kolumna
        for (int i = 0; i < provinces.length; i++)
        {
            provinces[i] = new CountryWithoutProvinces("Prowincja " + i);  // Przykładowa inicjalizacja
        }
        return provinces;
    }


    public static CountryColumns getCountryColumns(String firstRow,String countryName)
    {
        // Rozdzielamy pierwszy wiersz na kolumny
       String[] columns=firstRow.split(";");

        int firstColumnIndex = -1;  // Indeks pierwszej kolumny dla poszukiwanego kraju
        int columnCount = 0;  // Liczba kolumn dla danego kraju (w tym prowincji)

        // Szukamy indeksu pierwszej kolumny z państwem
        for (int i = 0; i < columns.length; i++)
        {
            // Sprawdzamy, czy kolumna to poszukiwane państwo
            if (columns[i].equalsIgnoreCase(countryName))//equalsIgnoreCase porównuje 2 c.znaków i zwraca booleana
            {
                firstColumnIndex = i;  // Zapisujemy indeks
                break;
            }
        }

        // Jeśli kraj nie zostanie znaleziony w pierwszym wierszu, zwracamy null
        if (firstColumnIndex == -1)
        {
            return null;
        }

        // Zliczamy liczbę kolumn związanych z tym państwem
        for (int i = firstColumnIndex + 1; i < columns.length; i++)
        {
            // Sprawdzamy prowincje
            if (!columns[i].isEmpty() && columns[i].startsWith(countryName))
            {
                columnCount++;
            }
            else
            {
                // Kończymy zliczanie, gdy napotkamy kolumnę, która nie jest powiązana z tym państwem
                break;
            }
        }

        // Zwracamy obiekt CountryColumns
        return new CountryColumns(firstColumnIndex, columnCount + 1);  // +1 dla samego państwa
    }

    //Abstrakcyjne metody(Zaimplementowane w klasach dziedziczących po Country)
    public abstract int getConfirmedCases(LocalDate date);
    public abstract int getDeaths(LocalDate date);

    // Statyczna metoda sortująca kraje według liczby zgonów w określonym przedziale dat
    public static List<Country> sortByDeaths(List<Country> countries, LocalDate first_date, LocalDate last_date)
    {
        // Tworzymy nową listę, aby nie modyfikować oryginalnej listy krajów
        List<Country> result = new ArrayList<>(countries);

        // Sortujemy listę wynikową przy użyciu niestandardowego komparatora
        result.sort((c1, c2) -> {
            int deaths1 = 0; // Suma zgonów dla pierwszego kraju
            int deaths2 = 0; // Suma zgonów dla drugiego kraju

            // Iterujemy po dniach w podanym zakresie (od first_date do last_date włącznie)
            for (LocalDate date = first_date; !date.isAfter(last_date); date = date.plusDays(1))
            {
                // Dodajemy liczbę zgonów danego dnia dla każdego kraju
                deaths1 += c1.getDeaths(date);
                deaths2 += c2.getDeaths(date);
            }

            // Porównujemy kraje według liczby zgonów (malejąco — kraj z większą liczbą zgonów ma być wyżej)
            return Integer.compare(deaths2, deaths1);
        });

        // Zwracamy posortowaną listę krajów
        return result;
    }

    public void saveToDataFile(String result_file_path) throws IOException
    {
        try(BufferedWriter writer=new BufferedWriter(new FileWriter(result_file_path)))
        {
            DateTimeFormatter formatter= DateTimeFormatter.ofPattern("d.MM.yy");


            writer.write("data/tprzypadki/tzgony");
            writer.newLine();


        }

    }








}
