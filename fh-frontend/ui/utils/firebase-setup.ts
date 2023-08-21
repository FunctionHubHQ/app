import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getAnalytics, isSupported } from "firebase/analytics";
import {offlineMode} from "#/ui/utils/headerConfig";

const firebaseConfig = {
  apiKey: "AIzaSyBMnVb8Ax2_F91lR9dnkOlJx7IByYlpcAw",
  authDomain: "functionhub-317d3.firebaseapp.com",
  projectId: "functionhub-317d3",
  storageBucket: "functionhub-317d3.appspot.com",
  messagingSenderId: "284285477716",
  appId: "1:284285477716:web:3d00aba788f6518036dec1",
  measurementId: "G-Z36KBJMDVV"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
export const auth = offlineMode ? null : getAuth(app);
const analyticsByEnv = process.env.NODE_ENV !== 'production' ? null :  isSupported().then(yes => yes ? getAnalytics(app) : null)
export const analytics =  analyticsByEnv
