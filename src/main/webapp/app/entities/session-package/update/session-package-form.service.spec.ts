import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../session-package.test-samples';

import { SessionPackageFormService } from './session-package-form.service';

describe('SessionPackage Form Service', () => {
  let service: SessionPackageFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SessionPackageFormService);
  });

  describe('Service methods', () => {
    describe('createSessionPackageFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createSessionPackageFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            price: expect.any(Object),
            credits: expect.any(Object),
            startDate: expect.any(Object),
            endDate: expect.any(Object),
            reviseCount: expect.any(Object),
            cancelCount: expect.any(Object),
          }),
        );
      });

      it('passing ISessionPackage should create a new form with FormGroup', () => {
        const formGroup = service.createSessionPackageFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            price: expect.any(Object),
            credits: expect.any(Object),
            startDate: expect.any(Object),
            endDate: expect.any(Object),
            reviseCount: expect.any(Object),
            cancelCount: expect.any(Object),
          }),
        );
      });
    });

    describe('getSessionPackage', () => {
      it('should return NewSessionPackage for default SessionPackage initial value', () => {
        const formGroup = service.createSessionPackageFormGroup(sampleWithNewData);

        const sessionPackage = service.getSessionPackage(formGroup) as any;

        expect(sessionPackage).toMatchObject(sampleWithNewData);
      });

      it('should return NewSessionPackage for empty SessionPackage initial value', () => {
        const formGroup = service.createSessionPackageFormGroup();

        const sessionPackage = service.getSessionPackage(formGroup) as any;

        expect(sessionPackage).toMatchObject({});
      });

      it('should return ISessionPackage', () => {
        const formGroup = service.createSessionPackageFormGroup(sampleWithRequiredData);

        const sessionPackage = service.getSessionPackage(formGroup) as any;

        expect(sessionPackage).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing ISessionPackage should not enable id FormControl', () => {
        const formGroup = service.createSessionPackageFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewSessionPackage should disable id FormControl', () => {
        const formGroup = service.createSessionPackageFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
