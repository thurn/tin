(ns tin.lexer-test
  (:require [clojure.test :refer :all]
            [tin.lexer :refer :all]
            [tin.common :as common]))

(deftest simpleMethodCall
  (is
   (=
    (tokenize-string "foo.bar(2)")
    ["LINE_START" "SYMBOL(foo)" "DOT" "SYMBOL(bar)" "LPAREN" "NUMBER(2)"
     "RPAREN" "LINE_START"])))

(deftest significantWhitespace
  (is
   (=
    (tokenize-string "foo.bar (2)")
    ["LINE_START" "SYMBOL(foo)" "DOT" "SYMBOL(bar)" "WS" "LPAREN" "NUMBER(2)"
     "RPAREN" "LINE_START"])))

(deftest initialNewline
  (is
   (=
    (tokenize-string "\nfoo")
    ["LINE_START" "SYMBOL(foo)" "LINE_START"])))

(deftest initialIndent
  (is
   (=
    (tokenize-string "  foo")
    ["INDENT" "LINE_START" "SYMBOL(foo)" "DEDENT" "LINE_START"])))

(deftest whitespaceLine
  (is
   (=
    (tokenize-string "foo\n     \nbar")
    ["LINE_START" "SYMBOL(foo)" "LINE_START" "SYMBOL(bar)" "LINE_START"])))

(deftest commentLine
  (is
   (=
    (tokenize-string "foo\n  // Comment\nbar")
    ["LINE_START" "SYMBOL(foo)" "LINE_START" "SYMBOL(bar)" "LINE_START"])))

(deftest blockCommentLine
  (is
   (=
    (tokenize-string "foo\n  /*Block Comment*/\nbar")
    ["LINE_START" "SYMBOL(foo)" "LINE_START" "SYMBOL(bar)" "LINE_START"])))

(deftest finalNewline
  (is
   (=
    (tokenize-string "foo\n\n")
    ["LINE_START" "SYMBOL(foo)" "LINE_START"])))

(deftest specialCharacters
  (is
   (=
    (tokenize-string "~@~#^:")
    ["LINE_START" "TILDE_AT" "TILDE" "POUND" "CARAT" "COLON" "LINE_START"])))

(deftest illegalDollarSign
  (is
   (common/failure?
    (tokenize-string "foo$bar"))))

(deftest numberVariants
  (is (= (tokenize-string "42") ["LINE_START" "NUMBER(42)" "LINE_START"]))
  (is (= (tokenize-string "12.3") ["LINE_START" "NUMBER(12.3)" "LINE_START"]))
  (is (= (tokenize-string "3e6") ["LINE_START" "NUMBER(3e6)" "LINE_START"]))
  (is (= (tokenize-string "6.2e3") ["LINE_START" "NUMBER(6.2e3)" "LINE_START"]))
  (is (= (tokenize-string "0xFA") ["LINE_START" "NUMBER(0xFA)" "LINE_START"]))
  (is (= (tokenize-string "0X1") ["LINE_START" "NUMBER(0X1)" "LINE_START"]))
  (is (= (tokenize-string "01") ["LINE_START" "NUMBER(01)" "LINE_START"])))

(deftest floatMethodCall
  (is
   (=
    (tokenize-string "1.2.3")
    ["LINE_START" "NUMBER(1.2)" "DOT" "NUMBER(3)" "LINE_START"])))

(deftest stringTest
  (is (=
       (tokenize-string "\"foo\"")
       ["LINE_START" "STRING(\"foo\")" "LINE_START"]))
  (is (=
       (tokenize-string "\"foo\\bar\"")
       ["LINE_START" "STRING(\"foo\\bar\")" "LINE_START"]))
  (is (=
       (tokenize-string "\"foo\\\"bar\"")
       ["LINE_START" "STRING(\"foo\\\"bar\")" "LINE_START"]))
  (is (=
       (tokenize-string "\"foo\n bar\"")
       ["LINE_START" "STRING(\"foo\n bar\")" "LINE_START"])))

(deftest codeStringTest
  (is (=
       (tokenize-string "`foo`")
       ["LINE_START" "CODE_STRING(`foo`)" "LINE_START"]))
  (is (=
       (tokenize-string "`foo\\bar`")
       ["LINE_START" "CODE_STRING(`foo\\bar`)" "LINE_START"]))
  (is (=
       (tokenize-string "`foo\\`bar`")
       ["LINE_START" "CODE_STRING(`foo\\`bar`)" "LINE_START"]))
  (is (=
       (tokenize-string "`foo\n bar`")
       ["LINE_START" "CODE_STRING(`foo\n bar`)" "LINE_START"])))

(deftest brackets
  (is
   (=
    (tokenize-string "({[}])")
    ["LINE_START" "LPAREN" "LBRACE" "LBRACKET" "RBRACE" "RBRACKET" "RPAREN"
     "LINE_START"])))

(deftest extraCloseFails
  (is (common/failure? (tokenize-string ")")))
  (is (common/failure? (tokenize-string "(foo))")))
  (is (common/failure? (tokenize-string "(fo]o)"))))

(deftest missingCloseFails
  (is (common/failure? (tokenize-string "(foo")))
  (is (common/failure? (tokenize-string "[")))
  (is (common/failure? (tokenize-string "((foo)"))))

(deftest delimitersIgnoreNewlines
  (is
   (=
    (tokenize-string "(foo\n  bar)\nfoo[foo\nbar]")
    ["LINE_START" "LPAREN" "SYMBOL(foo)" "WS" "SYMBOL(bar)" "RPAREN"
     "LINE_START" "SYMBOL(foo)" "LBRACKET" "SYMBOL(foo)" "WS" "SYMBOL(bar)"
     "RBRACKET" "LINE_START"])))
