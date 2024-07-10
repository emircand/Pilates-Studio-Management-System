import { IQRCode, NewQRCode } from './qr-code.model';

export const sampleWithRequiredData: IQRCode = {
  id: 13638,
  code: 'hmph pfft antiquity',
  sessionId: 'd7cbfc0d-ca23-41f4-808f-86029945e8df',
  athleteId: '998a4ffb-1dcc-4d73-96a0-18368b7b2eb2',
  coachId: '6d2411f0-89dc-46af-88bc-910b6d613dd6',
};

export const sampleWithPartialData: IQRCode = {
  id: 6864,
  code: 'nibble unless posit',
  sessionId: 'b12741dd-618d-48b3-9917-d1ebddaaf666',
  athleteId: '8f22fd0a-8c2d-4620-9e1b-bffcc28d2cb4',
  coachId: '04b8149c-52a6-4d31-8dc4-21823961b815',
};

export const sampleWithFullData: IQRCode = {
  id: 5596,
  code: 'angrily hence',
  sessionId: 'af974b48-eaf3-46d6-97dd-e25fec60ba49',
  athleteId: '2ac36b5a-2a70-4587-b276-5b22556ddcad',
  coachId: 'ce8d44a3-c8ba-40ce-b694-d50da1d500ef',
};

export const sampleWithNewData: NewQRCode = {
  code: 'ugh yearly',
  sessionId: 'b710cd2d-46dc-4eac-968d-33a19c84f9e3',
  athleteId: '4ad31917-d310-45f8-975b-cc5031a2eb16',
  coachId: '32fb6586-0df4-4dc9-a011-e606c78b4e6b',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
