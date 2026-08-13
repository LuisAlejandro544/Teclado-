#ifndef KEYBOARD_CORE_HPP
#define KEYBOARD_CORE_HPP

#include <string>
#include <vector>
#include <unordered_map>
#include <memory>
#include <algorithm>
#include <cctype>

namespace keyboard {

struct SuggestionCandidate {
    std::string text;
    float score;
    bool isExact;
};

// Trie node for high-speed prefix search and autocomplete
struct TrieNode {
    std::unordered_map<char, std::unique_ptr<TrieNode>> children;
    bool isEndOfWord = false;
    std::string word = "";
    int frequency = 0;
};

class KeyboardEngine {
private:
    std::unique_ptr<TrieNode> root;
    bool initialized = false;

    // Normalize Spanish accents for accent-insensitive search
    static std::string normalizeString(const std::string& input) {
        std::string result;
        result.reserve(input.size());
        for (size_t i = 0; i < input.size(); ++i) {
            unsigned char c = static_cast<unsigned char>(input[i]);
            if (c == 0xC3 && i + 1 < input.size()) {
                unsigned char next = static_cast<unsigned char>(input[i + 1]);
                // á, à, ä, â -> a
                if (next == 0xA1 || next == 0xA0 || next == 0xA4 || next == 0xA2) { result += 'a'; i++; }
                // é, è, ë, ê -> e
                else if (next == 0xA9 || next == 0xA8 || next == 0xAB || next == 0xAA) { result += 'e'; i++; }
                // í, ì, ï, î -> i
                else if (next == 0xAD || next == 0xAC || next == 0xAF || next == 0xAE) { result += 'i'; i++; }
                // ó, ò, ö, ô -> o
                else if (next == 0xB3 || next == 0xB2 || next == 0xB6 || next == 0xB4) { result += 'o'; i++; }
                // ú, ù, ü, û -> u
                else if (next == 0xBA || next == 0xB9 || next == 0xBC || next == 0xBB) { result += 'u'; i++; }
                // ñ -> ñ (preserve in utf8 or n)
                else if (next == 0xB1) { result += "\xC3\xB1"; i++; }
                else {
                    result += static_cast<char>(c);
                }
            } else {
                result += static_cast<char>(std::tolower(c));
            }
        }
        return result;
    }

    void collectAllWords(const TrieNode* node, std::vector<std::pair<std::string, int>>& results, int limit) const {
        if (!node) return;
        if (node->isEndOfWord) {
            results.push_back({node->word, node->frequency});
        }
        for (const auto& pair : node->children) {
            collectAllWords(pair.second.get(), results, limit);
        }
    }

    // Levenshtein distance for fuzzy matching and autocorrect
    static int computeEditDistance(const std::string& s1, const std::string& s2) {
        size_t len1 = s1.size(), len2 = s2.size();
        std::vector<std::vector<int>> d(len1 + 1, std::vector<int>(len2 + 1));

        for (size_t i = 0; i <= len1; ++i) d[i][0] = i;
        for (size_t j = 0; j <= len2; ++j) d[0][j] = j;

        for (size_t i = 1; i <= len1; ++i) {
            for (size_t j = 1; j <= len2; ++j) {
                int cost = (s1[i - 1] == s2[j - 1]) ? 0 : 1;
                d[i][j] = std::min({
                    d[i - 1][j] + 1,      // deletion
                    d[i][j - 1] + 1,      // insertion
                    d[i - 1][j - 1] + cost // substitution
                });
            }
        }
        return d[len1][len2];
    }

public:
    KeyboardEngine() : root(std::make_unique<TrieNode>()), initialized(false) {}

    void insertWord(const std::string& word, int frequency = 10) {
        if (word.empty()) return;
        TrieNode* current = root.get();
        std::string normalized = normalizeString(word);

        for (char c : normalized) {
            if (current->children.find(c) == current->children.end()) {
                current->children[c] = std::make_unique<TrieNode>();
            }
            current = current->children[c].get();
        }
        current->isEndOfWord = true;
        current->word = word; // Store original casing/accented form
        current->frequency = std::max(current->frequency, frequency);
    }

    void init() {
        if (initialized) return;

        // Load primary high-frequency Spanish dictionary into Trie
        const std::vector<std::pair<std::string, int>> spanishCorpus = {
            {"hola", 100}, {"que", 98}, {"qué", 97}, {"para", 95}, {"como", 94}, {"cómo", 93},
            {"está", 92}, {"esta", 91}, {"este", 90}, {"pero", 89}, {"todo", 88}, {"toda", 87},
            {"bien", 86}, {"bueno", 85}, {"buenos", 84}, {"buenas", 83}, {"días", 82}, {"tardes", 81},
            {"noches", 80}, {"gracias", 95}, {"por", 96}, {"favor", 90}, {"también", 88}, {"tambien", 70},
            {"donde", 85}, {"dónde", 86}, {"cuando", 85}, {"cuándo", 86}, {"porque", 89}, {"por qué", 87},
            {"tiempo", 80}, {"ahora", 84}, {"después", 78}, {"despues", 60}, {"siempre", 82}, {"nunca", 76},
            {"hacer", 84}, {"hecho", 75}, {"hace", 80}, {"hacerlo", 70}, {"amigo", 78}, {"amiga", 75},
            {"familia", 75}, {"trabajo", 80}, {"casa", 82}, {"vida", 78}, {"año", 80}, {"años", 82},
            {"día", 85}, {"hoy", 88}, {"mañana", 82}, {"ayer", 78}, {"semana", 76}, {"mes", 74},
            {"español", 85}, {"españa", 80}, {"méxico", 78}, {"argentina", 75}, {"colombia", 75},
            {"mundo", 79}, {"persona", 75}, {"personas", 76}, {"hombre", 74}, {"mujer", 74},
            {"niño", 72}, {"niña", 72}, {"nuevo", 78}, {"nueva", 77}, {"grande", 78}, {"pequeño", 75},
            {"pequeña", 74}, {"mismo", 76}, {"misma", 75}, {"otro", 78}, {"otra", 77}, {"otros", 76},
            {"otras", 75}, {"mucho", 82}, {"mucha", 80}, {"muchos", 78}, {"muchas", 77}, {"poco", 78},
            {"poca", 76}, {"pocos", 74}, {"pocas", 73}, {"algo", 80}, {"nada", 80}, {"alguien", 78},
            {"nadie", 76}, {"cada", 78}, {"ambos", 70}, {"ambas", 70}, {"primero", 75}, {"primera", 75},
            {"último", 75}, {"última", 74}, {"propio", 72}, {"propia", 72}, {"cierto", 70}, {"cierta", 70},
            {"único", 72}, {"única", 72}, {"posible", 75}, {"imposible", 68}, {"fácil", 78}, {"difícil", 76},
            {"rápido", 78}, {"lento", 70}, {"fuerte", 72}, {"claro", 80}, {"seguro", 78}, {"segura", 76},
            {"libre", 72}, {"juntos", 74}, {"juntas", 73}, {"solo", 80}, {"sólo", 78}, {"sola", 76},
            {"solos", 72}, {"solas", 70}, {"pronto", 78}, {"tarde", 78}, {"temprano", 72}, {"cerca", 76},
            {"lejos", 74}, {"aquí", 86}, {"acá", 80}, {"ahí", 82}, {"allí", 80}, {"allá", 78},
            {"arriba", 76}, {"abajo", 76}, {"delante", 72}, {"detrás", 74}, {"dentro", 76}, {"fuera", 76},
            {"mensaje", 85}, {"teclado", 90}, {"texto", 84}, {"pantalla", 80}, {"teléfono", 82},
            {"celular", 80}, {"móvil", 80}, {"correo", 78}, {"número", 80}, {"cuenta", 78},
            {"aplicación", 82}, {"app", 85}, {"foto", 80}, {"video", 80}, {"música", 80},
            {"enlace", 75}, {"código", 76}, {"sistema", 76}, {"información", 78}, {"respuesta", 80},
            {"pregunta", 80}, {"problema", 80}, {"solución", 80}, {"ayuda", 82}, {"gracias por", 80},
            {"un saludo", 75}, {"saludos", 78}, {"cuídate", 76}, {"abrazo", 76}, {"besos", 75},
            {"estoy", 86}, {"estás", 84}, {"estamos", 82}, {"están", 82}, {"estaba", 78},
            {"tengo", 84}, {"tienes", 82}, {"tenemos", 80}, {"tienen", 80}, {"tenía", 76},
            {"puedo", 84}, {"puedes", 82}, {"podemos", 80}, {"pueden", 80}, {"podría", 76},
            {"quiero", 84}, {"quieres", 82}, {"queremos", 80}, {"quieren", 80}, {"quisiera", 76},
            {"voy", 84}, {"vas", 82}, {"vamos", 85}, {"van", 80}, {"fui", 78}, {"fue", 82},
            {"sé", 80}, {"sabes", 82}, {"sabemos", 78}, {"saben", 78}, {"sabía", 74},
            {"digo", 78}, {"dices", 80}, {"dice", 82}, {"decimos", 76}, {"dicen", 78}, {"dije", 76},
            {"veo", 78}, {"ves", 78}, {"vemos", 78}, {"ven", 76}, {"vi", 76}, {"visto", 78},
            {"árbol", 70}, {"canción", 75}, {"corazón", 75}, {"avión", 70}, {"camión", 68},
            {"acción", 72}, {"atención", 75}, {"razón", 75}, {"opinión", 74}, {"dirección", 75}
        };

        for (const auto& item : spanishCorpus) {
            insertWord(item.first, item.second);
        }

        initialized = true;
    }

    std::vector<std::string> getSuggestions(const std::string& rawPrefix, int maxCount = 3) const {
        if (rawPrefix.empty()) {
            return {"hola", "gracias", "bueno"};
        }

        std::string normalized = normalizeString(rawPrefix);
        std::vector<std::string> results;

        // 1. Exact prefix matching in Trie
        const TrieNode* current = root.get();
        bool foundPrefix = true;
        for (char c : normalized) {
            auto it = current->children.find(c);
            if (it == current->children.end()) {
                foundPrefix = false;
                break;
            }
            current = it->second.get();
        }

        if (foundPrefix && current != nullptr) {
            std::vector<std::pair<std::string, int>> prefixMatches;
            collectAllWords(current, prefixMatches, 30);

            // Sort by frequency descending
            std::sort(prefixMatches.begin(), prefixMatches.end(),
                [](const std::pair<std::string, int>& a, const std::pair<std::string, int>& b) {
                    return a.second > b.second;
                });

            for (const auto& match : prefixMatches) {
                if (results.size() >= static_cast<size_t>(maxCount)) break;
                results.push_back(match.first);
            }
        }

        // 2. If fewer than maxCount, provide fuzzy autocorrect matches
        if (results.size() < static_cast<size_t>(maxCount)) {
            std::vector<std::pair<std::string, int>> allWords;
            collectAllWords(root.get(), allWords, 200);

            std::vector<std::pair<std::string, int>> fuzzyMatches;
            for (const auto& item : allWords) {
                std::string normWord = normalizeString(item.first);
                int dist = computeEditDistance(normalized, normWord);
                if (dist <= 2) {
                    // Check if already in results
                    if (std::find(results.begin(), results.end(), item.first) == results.end()) {
                        fuzzyMatches.push_back({item.first, item.second - (dist * 15)});
                    }
                }
            }

            std::sort(fuzzyMatches.begin(), fuzzyMatches.end(),
                [](const std::pair<std::string, int>& a, const std::pair<std::string, int>& b) {
                    return a.second > b.second;
                });

            for (const auto& fm : fuzzyMatches) {
                if (results.size() >= static_cast<size_t>(maxCount)) break;
                results.push_back(fm.first);
            }
        }

        // If still empty, return the raw input
        if (results.empty()) {
            results.push_back(rawPrefix);
        }

        return results;
    }

    std::string getAutocorrect(const std::string& rawWord) const {
        if (rawWord.empty()) return "";
        auto suggestions = getSuggestions(rawWord, 1);
        if (!suggestions.empty()) {
            return suggestions[0];
        }
        return rawWord;
    }

    // Audio / Voice Signal Processing for Autonomous On-Device Speech Processing
    static float computeRmsEnergy(const int16_t* audioData, size_t length) {
        if (!audioData || length == 0) return 0.0f;
        double sumSquares = 0.0;
        for (size_t i = 0; i < length; ++i) {
            double sample = static_cast<double>(audioData[i]);
            sumSquares += sample * sample;
        }
        double meanSquare = sumSquares / static_cast<double>(length);
        double rms = std::sqrt(meanSquare);
        // Normalize to dB-like scale 0.0f - 100.0f
        float db = 0.0f;
        if (rms > 0.0) {
            db = static_cast<float>(20.0 * std::log10(rms));
            if (db < 0.0f) db = 0.0f;
            if (db > 95.0f) db = 95.0f;
        }
        return db;
    }

    static bool detectVoiceActivity(const int16_t* audioData, size_t length, float thresholdDb = 35.0f) {
        float energy = computeRmsEnergy(audioData, length);
        return energy >= thresholdDb;
    }
};

} // namespace keyboard

#endif // KEYBOARD_CORE_HPP
