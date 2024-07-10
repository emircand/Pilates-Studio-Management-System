import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { ISessionPackage } from 'app/entities/session-package/session-package.model';
import { SessionPackageService } from 'app/entities/session-package/service/session-package.service';
import { AthleteService } from '../service/athlete.service';
import { IAthlete } from '../athlete.model';
import { AthleteFormService } from './athlete-form.service';

import { AthleteUpdateComponent } from './athlete-update.component';

describe('Athlete Management Update Component', () => {
  let comp: AthleteUpdateComponent;
  let fixture: ComponentFixture<AthleteUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let athleteFormService: AthleteFormService;
  let athleteService: AthleteService;
  let sessionPackageService: SessionPackageService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([]), AthleteUpdateComponent],
      providers: [
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(AthleteUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(AthleteUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    athleteFormService = TestBed.inject(AthleteFormService);
    athleteService = TestBed.inject(AthleteService);
    sessionPackageService = TestBed.inject(SessionPackageService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call sessionPackage query and add missing value', () => {
      const athlete: IAthlete = { id: 456 };
      const sessionPackage: ISessionPackage = { id: '86adda06-ae1c-474f-8aec-4a66b711816e' };
      athlete.sessionPackage = sessionPackage;

      const sessionPackageCollection: ISessionPackage[] = [{ id: '82cb6c93-61d1-43d8-bc8d-a69d423c3366' }];
      jest.spyOn(sessionPackageService, 'query').mockReturnValue(of(new HttpResponse({ body: sessionPackageCollection })));
      const expectedCollection: ISessionPackage[] = [sessionPackage, ...sessionPackageCollection];
      jest.spyOn(sessionPackageService, 'addSessionPackageToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ athlete });
      comp.ngOnInit();

      expect(sessionPackageService.query).toHaveBeenCalled();
      expect(sessionPackageService.addSessionPackageToCollectionIfMissing).toHaveBeenCalledWith(sessionPackageCollection, sessionPackage);
      expect(comp.sessionPackagesCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const athlete: IAthlete = { id: 456 };
      const sessionPackage: ISessionPackage = { id: '754488d9-e0c8-43a4-b0d2-f57e3e01b4cd' };
      athlete.sessionPackage = sessionPackage;

      activatedRoute.data = of({ athlete });
      comp.ngOnInit();

      expect(comp.sessionPackagesCollection).toContain(sessionPackage);
      expect(comp.athlete).toEqual(athlete);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IAthlete>>();
      const athlete = { id: 123 };
      jest.spyOn(athleteFormService, 'getAthlete').mockReturnValue(athlete);
      jest.spyOn(athleteService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ athlete });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: athlete }));
      saveSubject.complete();

      // THEN
      expect(athleteFormService.getAthlete).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(athleteService.update).toHaveBeenCalledWith(expect.objectContaining(athlete));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IAthlete>>();
      const athlete = { id: 123 };
      jest.spyOn(athleteFormService, 'getAthlete').mockReturnValue({ id: null });
      jest.spyOn(athleteService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ athlete: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: athlete }));
      saveSubject.complete();

      // THEN
      expect(athleteFormService.getAthlete).toHaveBeenCalled();
      expect(athleteService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IAthlete>>();
      const athlete = { id: 123 };
      jest.spyOn(athleteService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ athlete });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(athleteService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareSessionPackage', () => {
      it('Should forward to sessionPackageService', () => {
        const entity = { id: 'ABC' };
        const entity2 = { id: 'CBA' };
        jest.spyOn(sessionPackageService, 'compareSessionPackage');
        comp.compareSessionPackage(entity, entity2);
        expect(sessionPackageService.compareSessionPackage).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
