import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../athlete.test-samples';

import { AthleteFormService } from './athlete-form.service';

describe('Athlete Form Service', () => {
  let service: AthleteFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AthleteFormService);
  });

  describe('Service methods', () => {
    describe('createAthleteFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createAthleteFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            email: expect.any(Object),
            phone: expect.any(Object),
            city: expect.any(Object),
            address: expect.any(Object),
            birthday: expect.any(Object),
            sessionPackage: expect.any(Object),
          }),
        );
      });

      it('passing IAthlete should create a new form with FormGroup', () => {
        const formGroup = service.createAthleteFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            email: expect.any(Object),
            phone: expect.any(Object),
            city: expect.any(Object),
            address: expect.any(Object),
            birthday: expect.any(Object),
            sessionPackage: expect.any(Object),
          }),
        );
      });
    });

    describe('getAthlete', () => {
      it('should return NewAthlete for default Athlete initial value', () => {
        const formGroup = service.createAthleteFormGroup(sampleWithNewData);

        const athlete = service.getAthlete(formGroup) as any;

        expect(athlete).toMatchObject(sampleWithNewData);
      });

      it('should return NewAthlete for empty Athlete initial value', () => {
        const formGroup = service.createAthleteFormGroup();

        const athlete = service.getAthlete(formGroup) as any;

        expect(athlete).toMatchObject({});
      });

      it('should return IAthlete', () => {
        const formGroup = service.createAthleteFormGroup(sampleWithRequiredData);

        const athlete = service.getAthlete(formGroup) as any;

        expect(athlete).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IAthlete should not enable id FormControl', () => {
        const formGroup = service.createAthleteFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewAthlete should disable id FormControl', () => {
        const formGroup = service.createAthleteFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
