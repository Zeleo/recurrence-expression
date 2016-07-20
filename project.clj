(defproject com.bjondinc/recurrence-expression "0.2.7"
  :description "JSON for expressing recurrence patterns"
  :url "https://github.com/Bjond/recurrence-expression"
  :scm "https://github.com/Bjond/recurrence-expression.git"
  :license {:name "GNU Lesser General Public License v3.0"
            :url "http://www.gnu.org/licenses/lgpl-3.0.txt"
            :year 2015
            :key "lgpl-3.0"}
  :plugins [[lein-aot-filter "0.1.0"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [clj-time "0.8.0"]
                 [prismatic/schema "0.4.0"]
                 [cheshire "5.6.3"]]
  :deploy-repositories [["releases" {:url "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                                     :creds :gpg}]
                         ["snapshots" {:url "https://oss.sonatype.org/content/repositories/snapshots/"
                                       :creds :gpg}]]
  :pom-addition [:developers [:developer
                              [:name "Michi Oshima"]
                              [:email "michi.oshima@bjondinc.com"]
                              [:timezone "-5"]]]
  :aot :all
  :aot-include [#"^com.*"])
