#define numleds 2
#define pins 1

int RED[numleds];
int GREEN[numleds];
int BLUE[numleds];

int clockpins[pins];
int datapins[pins];

void setup()
{
  clockpins[0] = 2;
  clockpins[1] = 4;
  datapins[0] = 3;
  datapins[1] = 5;
  for (int i = 0; i < pins; i++)
  {
    pinMode(clockpins[i], OUTPUT);
    pinMode(datapins[i], OUTPUT);
  }
  setColor(255, 255, 255);
  updateLEDTape();
  Serial.begin(9600);
}

void loop()
{
  recvOneChar();
  delay(100);
}

byte bytes[1];

void recvOneChar()
{
  if (Serial.available() > 0)
  {
    Serial.readBytes(bytes, 1);
    toggleLED(bytes[0]);
  }
}

void toggleLED(byte b)
{
  if (b == 1)
  {
    setColor(255, 255, 255);
    updateLEDTape();
  }
  else if (b == 2)
  {
    setColor(0, 0, 0);
    updateLEDTape();
  }
}

void updateLEDTape()
{
  for (int j = 0; j < pins; j++)
  {
    for (int i = 0; i < numleds; i++)
    {
      shiftOut(datapins[j], clockpins[j], MSBFIRST, RED[i]);
      shiftOut(datapins[j], clockpins[j], MSBFIRST, BLUE[i]);
      shiftOut(datapins[j], clockpins[j], MSBFIRST, GREEN[i]);
    }
  }
}

void setColor(int r, int g, int b)
{
  for (int i = 0; i < numleds; i++)
  {
    RED[i] = r;
    GREEN[i] = g;
    BLUE[i] = b;
  }
}
