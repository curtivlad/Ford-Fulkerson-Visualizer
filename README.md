# FlowViz - Ford-Fulkerson Visualizer

O aplicație Java interactivă pentru vizualizarea algoritmului **Ford-Fulkerson** pentru calculul fluxului maxim și tăieturii minime într-o rețea de flux.

## 📋 Descriere

Acest program permite utilizatorilor să:
- Construiască grafuri direcționate interactiv
- Definească capacități și fluxuri inițiale pe arce
- Calculeze fluxul maxim între două noduri folosind algoritmul Ford-Fulkerson (cu BFS - Edmonds-Karp)
- Vizualizeze tăietura minimă (min-cut) evidențiată cu roșu

## 🚀 Funcționalități

### Interacțiune cu mouse-ul
| Acțiune | Efect |
|---------|-------|
| **Click simplu** în spațiu gol | Adaugă un nod nou |
| **Drag** de pe un nod pe altul | Creează un arc (muchie direcționată) |
| **Drag** nod în spațiu gol | Mută nodul |
| **Double-click** pe nod | Șterge nodul și arcele conectate |

### Scurtături de tastatură
| Tastă | Acțiune |
|-------|---------|
| `F` | Calculează Flux Maxim |
| `R` | Resetează Fluxurile |
| `C` | Șterge Tot (Clear) |

## 🛠️ Tehnologii folosite

- **Java 11+**
- **Swing** - pentru interfața grafică
- **Maven** - pentru build și management dependențe

## 📦 Instalare și Rulare

### Cerințe
- Java JDK 11 sau mai nou
- Maven 3.x

### Compilare
```bash
mvn clean compile
```

### Rulare
```bash
mvn exec:java -Dexec.mainClass="org.example.Main"
```

### Creare JAR executabil
```bash
mvn package
java -jar target/ford-fulkerson-1.0-SNAPSHOT.jar
```

## 📐 Algoritm Ford-Fulkerson

### Cum funcționează

1. **Inițializare**: Toate fluxurile încep de la 0 (sau valori inițiale specificate)

2. **Căutare drum de augmentare**: Se folosește BFS (Breadth-First Search) pentru a găsi un drum de la sursă la destinație în graful rezidual (varianta Edmonds-Karp)

3. **Augmentare**: Se găsește capacitatea reziduală minimă pe drum și se actualizează fluxurile

4. **Repetare**: Se repetă până nu mai există drumuri de augmentare

5. **Tăietură minimă**: După terminare, nodurile accesibile din sursă în graful rezidual formează o partiție; arcele care traversează această partiție constituie tăietura minimă

### Structuri de date principale

```java
private int vertices;              // Numărul de noduri
private List<List<EdgeFF>> graph;  // Lista de adiacență cu arce
private Map<Edge, Integer> flows;  // Fluxurile finale pe fiecare arc
```

- `vertices` - numărul total de noduri din graf
- `graph` - reprezintă graful ca listă de adiacență; fiecare nod are o listă de arce (`EdgeFF`)
- `flows` - dicționar care mapează fiecare arc la fluxul său final calculat

### Clasa EdgeFF (Arc intern)
```java
static class EdgeFF {
    int to;           // Nodul destinație
    int capacity;     // Capacitatea arcului
    int flow;         // Fluxul curent
    EdgeFF reverse;   // Referință către arcul invers (pentru graful rezidual)
}
```

## 📊 Exemplu de utilizare

1. **Creați noduri**: Click în diferite locuri pentru a adăuga nodurile 0, 1, 2, 3...

2. **Conectați-le**: Trageți de la un nod la altul pentru a crea arce și introduceți capacitatea

3. **Calculați**: Apăsați `F` sau butonul "Calculează Flux Maxim"

4. **Introduceți sursa și destinația**: De exemplu, sursă = 0, destinație = 3

5. **Vizualizați rezultatul**: 
   - Fluxurile apar pe fiecare arc (flux/capacitate)
   - Arcele din tăietura minimă sunt evidențiate cu roșu

## 📈 Complexitate

- **Timp**: O(V × E²) pentru varianta Edmonds-Karp (cu BFS)
- **Spațiu**: O(V + E) pentru stocarea grafului

## 👨‍💻 Autor

Proiect realizat pentru cursul de **Algoritmica Grafurilor** - Anul 2

## 📄 Licență

Proiect educațional - liber pentru utilizare și modificare.

