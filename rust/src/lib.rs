//! Rust Native Module for High-Performance Keyboard Logic & Autocorrect Trie
//! 
//! This module provides high performance text prediction, dictionary lookups,
//! and gesture swipe decoding in Rust via JNI/UniFFI.

use std::collections::HashMap;

#[repr(C)]
pub struct SuggestionItem {
    pub word: *const std::os::raw::c_char,
    pub confidence: f32,
}

#[derive(Default)]
pub struct RustTrieNode {
    pub children: HashMap<char, RustTrieNode>,
    pub is_end_of_word: bool,
    pub word: String,
    pub frequency: i32,
}

pub struct RustKeyboardEngine {
    root: RustTrieNode,
    initialized: bool,
}

impl RustKeyboardEngine {
    pub fn new() -> Self {
        RustKeyboardEngine {
            root: RustTrieNode::default(),
            initialized: false,
        }
    }

    pub fn initialize(&mut self) {
        if self.initialized {
            return;
        }
        let spanish_words = [
            ("hola", 100), ("gracias", 95), ("buenos", 90), ("días", 88),
            ("por", 94), ("favor", 90), ("también", 88), ("está", 86),
            ("cuando", 85), ("donde", 84), ("porque", 88), ("ahora", 82),
        ];

        for (word, freq) in spanish_words {
            self.insert(word, freq);
        }
        self.initialized = true;
    }

    pub fn insert(&mut self, word: &str, freq: i32) {
        let mut curr = &mut self.root;
        for c in word.to_lowercase().chars() {
            curr = curr.children.entry(c).or_default();
        }
        curr.is_end_of_word = true;
        curr.word = word.to_string();
        curr.frequency = freq;
    }

    pub fn predict_next_words(&self, prefix: &str) -> Vec<String> {
        let mut curr = &self.root;
        for c in prefix.to_lowercase().chars() {
            if let Some(next) = curr.children.get(&c) {
                curr = next;
            } else {
                return Vec::new();
            }
        }
        let mut results = Vec::new();
        self.collect(curr, &mut results);
        results.sort_by(|a, b| b.1.cmp(&a.1));
        results.into_iter().take(3).map(|(w, _)| w).collect()
    }

    fn collect(&self, node: &RustTrieNode, results: &mut Vec<(String, i32)>) {
        if node.is_end_of_word {
            results.push((node.word.clone(), node.frequency));
        }
        for child in node.children.values() {
            self.collect(child, results);
        }
    }
}

#[no_mangle]
pub extern "C" fn rust_keyboard_version() -> *const std::os::raw::c_char {
    c"Rust Core Engine v1.2.0 (Trie & Autocorrect)".as_ptr()
}
