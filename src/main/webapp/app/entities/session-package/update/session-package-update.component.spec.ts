import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { SessionPackageService } from '../service/session-package.service';
import { ISessionPackage } from '../session-package.model';
import { SessionPackageFormService } from './session-package-form.service';

import { SessionPackageUpdateComponent } from './session-package-update.component';

describe('SessionPackage Management Update Component', () => {
  let comp: SessionPackageUpdateComponent;
  let fixture: ComponentFixture<SessionPackageUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let sessionPackageFormService: SessionPackageFormService;
  let sessionPackageService: SessionPackageService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([]), SessionPackageUpdateComponent],
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
      .overrideTemplate(SessionPackageUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(SessionPackageUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    sessionPackageFormService = TestBed.inject(SessionPackageFormService);
    sessionPackageService = TestBed.inject(SessionPackageService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should update editForm', () => {
      const sessionPackage: ISessionPackage = { id: 'CBA' };

      activatedRoute.data = of({ sessionPackage });
      comp.ngOnInit();

      expect(comp.sessionPackage).toEqual(sessionPackage);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ISessionPackage>>();
      const sessionPackage = { id: 'ABC' };
      jest.spyOn(sessionPackageFormService, 'getSessionPackage').mockReturnValue(sessionPackage);
      jest.spyOn(sessionPackageService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ sessionPackage });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: sessionPackage }));
      saveSubject.complete();

      // THEN
      expect(sessionPackageFormService.getSessionPackage).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(sessionPackageService.update).toHaveBeenCalledWith(expect.objectContaining(sessionPackage));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ISessionPackage>>();
      const sessionPackage = { id: 'ABC' };
      jest.spyOn(sessionPackageFormService, 'getSessionPackage').mockReturnValue({ id: null });
      jest.spyOn(sessionPackageService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ sessionPackage: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: sessionPackage }));
      saveSubject.complete();

      // THEN
      expect(sessionPackageFormService.getSessionPackage).toHaveBeenCalled();
      expect(sessionPackageService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ISessionPackage>>();
      const sessionPackage = { id: 'ABC' };
      jest.spyOn(sessionPackageService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ sessionPackage });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(sessionPackageService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
