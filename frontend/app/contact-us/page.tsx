import Breadcrumb from "@/components/Common/Breadcrumb";
const ContactUs = () => {
  return (
      <>
        <Breadcrumb
            pageName="Contact Us"
         description=""/>
        <section id="contact-us" className="pt-4 md:pt-5 lg:pt-6">
          <div className="container">
            <div className="border-b border-body-color/[.15] pb-16 dark:border-white/[.15] md:pb-20 lg:pb-28">
              <div className="-mx-4 flex flex-wrap items-center">
                <div className="body-container">
                  <p>If you have any questions or concerns, you can contact us:</p>
                  <ul>
                    <li>By email: shoppiem team at gmail dot com</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </section>
      </>
  );
};

export default ContactUs;
