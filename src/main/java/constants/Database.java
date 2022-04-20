package constants;

import main.Config;

public enum Database {
    ;

    public enum Mongo {
        MAIN("Learning0"),
        SUPPORTIFY_DATABASE(Config.get(ENV.MONGO_DATABASE_NAME)),
        SUPPORTIFY_GUILDS("guilds");

        // Collections


        private final String str;

        Mongo(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }

        public static String getConnectionString(String db) {
            return "mongodb+srv://" + Config.get(ENV.MONGO_USERNAME) + ":" + Config.get(ENV.MONGO_PASSWORD) + "@"+Config.get(ENV.MONGO_CLUSTER_NAME)+"."+Config.get(ENV.MONGO_HOSTNAME)+"/" + db + "?retryWrites=true&w=majority";
        }
    }
}
