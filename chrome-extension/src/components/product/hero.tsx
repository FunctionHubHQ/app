import React, {FC} from "react";

const Hero: FC<any> =  (_) => {

  return (
      <div>
        <div className="disclaimer">
          Shoppiem is currently in Beta. If you would like to get in touch with us, please send us an email at shoppiemteam@gmail.com.
        </div>
        <div className="break-2"></div>
        <div className="section-header">
          Shoppiem is a virtual assistant that helps you save time and money by answering your Amazon product questions instantly.
        </div>
        <div className="break-2"></div>
        <div className="landing-section-header">
          💡<span>Open an Amazon product page to use Shoppiem</span>
        </div>
        {/*<div className="break-1"></div>*/}
        {/*<div className="landing-section">*/}
        {/*  Shoppiem combines data on the specific product you are interested in from Amazon and all over the Internet to provide you with accurate information.*/}
        {/*</div>*/}
        <div className="break-1"></div>
        <div className="landing-section-header">
          🙏<span>Please give us a feedback</span>
        </div>
        <div className="break-1"></div>
        <div className="landing-section">
          Please give us a review in the Chrome Store if you liked using Shoppiem and tell your friends! Send us a feedback right from the chat box below by starting your message with the word "Feedback". Thank you!
        </div>
      </div>
  );
}
export default Hero