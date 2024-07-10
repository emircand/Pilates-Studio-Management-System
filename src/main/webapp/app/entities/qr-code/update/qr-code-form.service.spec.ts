import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../qr-code.test-samples';

import { QRCodeFormService } from './qr-code-form.service';

describe('QRCode Form Service', () => {
  let service: QRCodeFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(QRCodeFormService);
  });

  describe('Service methods', () => {
    describe('createQRCodeFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createQRCodeFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            code: expect.any(Object),
            sessionId: expect.any(Object),
            athleteId: expect.any(Object),
            coachId: expect.any(Object),
          }),
        );
      });

      it('passing IQRCode should create a new form with FormGroup', () => {
        const formGroup = service.createQRCodeFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            code: expect.any(Object),
            sessionId: expect.any(Object),
            athleteId: expect.any(Object),
            coachId: expect.any(Object),
          }),
        );
      });
    });

    describe('getQRCode', () => {
      it('should return NewQRCode for default QRCode initial value', () => {
        const formGroup = service.createQRCodeFormGroup(sampleWithNewData);

        const qRCode = service.getQRCode(formGroup) as any;

        expect(qRCode).toMatchObject(sampleWithNewData);
      });

      it('should return NewQRCode for empty QRCode initial value', () => {
        const formGroup = service.createQRCodeFormGroup();

        const qRCode = service.getQRCode(formGroup) as any;

        expect(qRCode).toMatchObject({});
      });

      it('should return IQRCode', () => {
        const formGroup = service.createQRCodeFormGroup(sampleWithRequiredData);

        const qRCode = service.getQRCode(formGroup) as any;

        expect(qRCode).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IQRCode should not enable id FormControl', () => {
        const formGroup = service.createQRCodeFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewQRCode should disable id FormControl', () => {
        const formGroup = service.createQRCodeFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
