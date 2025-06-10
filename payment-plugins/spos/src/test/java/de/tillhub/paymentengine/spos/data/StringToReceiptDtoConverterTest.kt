package de.tillhub.paymentengine.spos.data

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class StringToReceiptDtoConverterTest : FunSpec({

    test("convert") {
        val converter = StringToReceiptDtoConverter()

        val result = converter.convert(RECEIPT_XML)

        result.toReceiptString() shouldBe RECEIPT
    }
}) {
    companion object {
        private const val RECEIPT_XML = """
<Receipt numCols="42">
   <ReceiptLine type="TEXT">
      <Formats>
         <Format from="0" to="42">NORMAL</Format>
      </Formats>
      <Text>       - Kundenbeleg -</Text>
   </ReceiptLine>
   <ReceiptLine type="TEXT">
      <Formats>
         <Format from="0" to="42">NORMAL</Format>
      </Formats>
      <Text>Testterminal SPOS</Text>
   </ReceiptLine>
   <ReceiptLine type="TEXT">
      <Formats>
         <Format from="0" to="42">NORMAL</Format>
      </Formats>
      <Text> </Text>
   </ReceiptLine>
   <ReceiptLine type="TEXT">
      <Formats>
         <Format from="0" to="42">NORMAL</Format>
      </Formats>
      <Text>        Kartenzahlung</Text>
   </ReceiptLine>
   <ReceiptLine type="TEXT">
      <Formats>
         <Format from="0" to="42">NORMAL</Format>
      </Formats>
      <Text>         VISA CREDIT</Text>
   </ReceiptLine>
   <ReceiptLine type="TEXT">
      <Formats>
         <Format from="0" to="42">NORMAL</Format>
      </Formats>
      <Text> </Text>
   </ReceiptLine>
   <ReceiptLine type="TEXT">
      <Formats>
         <Format from="0" to="42">NORMAL</Format>
      </Formats>
      <Text>Betrag:               EUR 6,00</Text>
   </ReceiptLine>
   <ReceiptLine type="TEXT">
      <Formats>
         <Format from="0" to="42">NORMAL</Format>
      </Formats>
      <Text> </Text>
   </ReceiptLine>
   <ReceiptLine type="TEXT">
      <Formats>
         <Format from="0" to="42">NORMAL</Format>
      </Formats>
      <Text>Datum: 14.11.24    Zeit: 14:03</Text>
   </ReceiptLine>
   <ReceiptLine type="TEXT">
      <Formats>
         <Format from="0" to="42">NORMAL</Format>
      </Formats>
      <Text>Terminal-Nr.:         55754825</Text>
   </ReceiptLine>
   <ReceiptLine type="TEXT">
      <Formats>
         <Format from="0" to="42">NORMAL</Format>
      </Formats>
      <Text>Trace-Nr.:              000014</Text>
   </ReceiptLine>
   <ReceiptLine type="TEXT">
      <Formats>
         <Format from="0" to="42">NORMAL</Format>
      </Formats>
      <Text>Beleg:                       6</Text>
   </ReceiptLine>
   <ReceiptLine type="TEXT">
      <Formats>
         <Format from="0" to="42">NORMAL</Format>
      </Formats>
      <Text>Karten-Nr.:   ############0010</Text>
   </ReceiptLine>
   <ReceiptLine type="TEXT">
      <Formats>
         <Format from="0" to="42">NORMAL</Format>
      </Formats>
      <Text>Folge-Nr.:                0001</Text>
   </ReceiptLine>
   <ReceiptLine type="TEXT">
      <Formats>
         <Format from="0" to="42">NORMAL</Format>
      </Formats>
      <Text>Kartendaten:        Kontaktlos</Text>
   </ReceiptLine>
   <ReceiptLine type="TEXT">
      <Formats>
         <Format from="0" to="42">NORMAL</Format>
      </Formats>
      <Text>AID:                    096299</Text>
   </ReceiptLine>
   <ReceiptLine type="TEXT">
      <Formats>
         <Format from="0" to="42">NORMAL</Format>
      </Formats>
      <Text>VU-Nr.:           455600152753</Text>
   </ReceiptLine>
   <ReceiptLine type="TEXT">
      <Formats>
         <Format from="0" to="42">NORMAL</Format>
      </Formats>
      <Text>EMV-AID:        A0000000031010</Text>
   </ReceiptLine>
   <ReceiptLine type="TEXT">
      <Formats>
         <Format from="0" to="42">NORMAL</Format>
      </Formats>
      <Text> </Text>
   </ReceiptLine>
   <ReceiptLine type="TEXT">
      <Formats>
         <Format from="0" to="42">NORMAL</Format>
      </Formats>
      <Text>       Zahlung erfolgt</Text>
   </ReceiptLine>
</Receipt>
        """

        const val RECEIPT = """       - Kundenbeleg -
Testterminal SPOS
 
        Kartenzahlung
         VISA CREDIT
 
Betrag:               EUR 6,00
 
Datum: 14.11.24    Zeit: 14:03
Terminal-Nr.:         55754825
Trace-Nr.:              000014
Beleg:                       6
Karten-Nr.:   ############0010
Folge-Nr.:                0001
Kartendaten:        Kontaktlos
AID:                    096299
VU-Nr.:           455600152753
EMV-AID:        A0000000031010
 
       Zahlung erfolgt
"""
    }
}
